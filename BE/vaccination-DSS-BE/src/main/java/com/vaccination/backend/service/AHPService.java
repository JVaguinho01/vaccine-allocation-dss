package com.vaccination.backend.service;

import com.vaccination.backend.dto.AllocationResponseDTO;
import com.vaccination.backend.dto.ConsistencyDTO;
import com.vaccination.backend.dto.InstitutionCriteriaDTO;
import com.vaccination.backend.dto.WeightDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AHPService {

    public AllocationResponseDTO calculateWeights(
            List<InstitutionCriteriaDTO> institutions,
            List<Integer> criteriaOrder
    ) {

        double[][] pairwiseMatrix =
                buildRankingMatrix(criteriaOrder);

        double[] weightsArray =
                calculateWeightsFromMatrix(pairwiseMatrix);

        ConsistencyDTO consistency =
                calculateConsistency(
                        pairwiseMatrix,
                        weightsArray
                );

        List<WeightDTO> weights =
                new ArrayList<>();

        weights.add(
                new WeightDTO(
                        "Demand",
                        round(weightsArray[0])
                )
        );

        weights.add(
                new WeightDTO(
                        "Risk Level",
                        round(weightsArray[1])
                )
        );

        weights.add(
                new WeightDTO(
                        "Risk Exposure",
                        round(weightsArray[2])
                )
        );

        weights.add(
                new WeightDTO(
                        "Logistic Cost",
                        round(weightsArray[3])
                )
        );

        weights.add(
                new WeightDTO(
                        "Capacity",
                        round(weightsArray[4])
                )
        );

        weights.add(
                new WeightDTO(
                        "Waste Risk",
                        round(weightsArray[5])
                )
        );

        AllocationResponseDTO response =
                new AllocationResponseDTO();

        response.setWeights(weights);

        response.setConsistency(consistency);

        return response;
    }

    public void applyScores(
            List<InstitutionCriteriaDTO> institutions,
            List<WeightDTO> weights
    ) {

        double rawMaxDemand = institutions.stream()
                .mapToDouble(InstitutionCriteriaDTO::getDemand)
                .max().orElse(0);

        double rawMaxRiskLevel = institutions.stream()
                .mapToDouble(InstitutionCriteriaDTO::getRiskLevel)
                .max().orElse(0);

        double rawMaxRiskExposure = institutions.stream()
                .mapToDouble(InstitutionCriteriaDTO::getRiskExposure)
                .max().orElse(0);

        // Criteria with no data get weight 0; the rest are renormalised to sum to 1.
        double effDemand      = rawMaxDemand      > 0 ? weights.get(0).getWeight() : 0;
        double effRiskLevel   = rawMaxRiskLevel   > 0 ? weights.get(1).getWeight() : 0;
        double effRiskExposure= rawMaxRiskExposure> 0 ? weights.get(2).getWeight() : 0;
        double effCost        = weights.get(3).getWeight();
        double effCapacity    = weights.get(4).getWeight();
        double effWaste       = weights.get(5).getWeight();

        double total = effDemand + effRiskLevel + effRiskExposure
                + effCost + effCapacity + effWaste;

        if (total > 0) {
            effDemand       /= total;
            effRiskLevel    /= total;
            effRiskExposure /= total;
            effCost         /= total;
            effCapacity     /= total;
            effWaste        /= total;
        }

        weights.get(0).setWeight(round(effDemand));
        weights.get(1).setWeight(round(effRiskLevel));
        weights.get(2).setWeight(round(effRiskExposure));
        weights.get(3).setWeight(round(effCost));
        weights.get(4).setWeight(round(effCapacity));
        weights.get(5).setWeight(round(effWaste));

        double maxDemand       = rawMaxDemand      > 0 ? rawMaxDemand      : 1;
        double maxRiskLevel    = rawMaxRiskLevel   > 0 ? rawMaxRiskLevel   : 1;
        double maxRiskExposure = rawMaxRiskExposure> 0 ? rawMaxRiskExposure: 1;

        double minCost = minPositive(
                institutions.stream()
                        .mapToDouble(InstitutionCriteriaDTO::getLogisticCost)
                        .toArray()
        );

        double maxCapacity = max(
                institutions.stream()
                        .mapToDouble(InstitutionCriteriaDTO::getCapacity)
                        .toArray()
        );

        for (InstitutionCriteriaDTO institution : institutions) {

            double demandNorm =
                    safeDivide(institution.getDemand(), maxDemand);

            double riskLevelNorm =
                    safeDivide(institution.getRiskLevel(), maxRiskLevel);

            double riskExposureNorm =
                    safeDivide(institution.getRiskExposure(), maxRiskExposure);

            double capacityNorm =
                    safeDivide(institution.getCapacity(), maxCapacity);

            // Inverted: cheapest gets 1.0; cost 0 means the institution is the warehouse.
            double cost = institution.getLogisticCost();
            double costNorm = (cost <= 0)
                    ? 1.0
                    : safeDivide(minCost, cost);

            double wasteNorm = 1.0 - institution.getWasteRisk();

            double finalScore =
                    effDemand       * demandNorm +
                    effRiskLevel    * riskLevelNorm +
                    effRiskExposure * riskExposureNorm +
                    effCost         * costNorm +
                    effCapacity     * capacityNorm +
                    effWaste        * wasteNorm;

            if (Double.isNaN(finalScore) || Double.isInfinite(finalScore)) {
                finalScore = 0.0;
            }

            institution.setFinalScore(round(finalScore));
        }
    }

    private double[][] buildRankingMatrix(
            List<Integer> ranking
    ) {

        int n = ranking.size();

        double[][] matrix =
                new double[n][n];

        for (int i = 0; i < n; i++) {

            for (int j = 0; j < n; j++) {

                if (i == j) {

                    matrix[i][j] = 1.0;

                } else {

                    int priorityI =
                            ranking.indexOf(i + 1);

                    int priorityJ =
                            ranking.indexOf(j + 1);

                    int difference =
                            priorityJ - priorityI;

                    double value =
                            Math.min(
                                    Math.abs(difference) + 1,
                                    9
                            );

                    if (priorityI < priorityJ) {

                        matrix[i][j] = value;

                    } else {

                        matrix[i][j] = 1.0 / value;
                    }
                }
            }
        }

        return matrix;
    }

    private double[] calculateWeightsFromMatrix(
            double[][] matrix
    ) {

        int n = matrix.length;

        double[] weights = new double[n];

        double sum = 0.0;

        for (int i = 0; i < n; i++) {

            double product = 1.0;

            for (int j = 0; j < n; j++) {

                product *= matrix[i][j];
            }

            weights[i] =
                    Math.pow(product, 1.0 / n);

            sum += weights[i];
        }

        for (int i = 0; i < n; i++) {

            weights[i] =
                    weights[i] / sum;
        }

        return weights;
    }

    private double max(double[] values) {

        return Math.max(
                Arrays.stream(values)
                        .max()
                        .orElse(1),
                1
        );
    }

    private double minPositive(double[] values) {

        return Arrays.stream(values)
                .filter(v -> v > 0)
                .min()
                .orElse(1);
    }

    private double safeDivide(
            double numerator,
            double denominator
    ) {

        if (denominator == 0 ||
                Double.isNaN(denominator) ||
                Double.isInfinite(denominator)) {

            return 0;
        }

        double result =
                numerator / denominator;

        if (Double.isNaN(result) ||
                Double.isInfinite(result)) {

            return 0;
        }

        return result;
    }

    private double round(double value) {

        return Math.round(value * 10000.0)
                / 10000.0;
    }

    private static final double[] RANDOM_INDEX = {
            0.0,
            0.0,
            0.58,
            0.90,
            1.12,
            1.24,
            1.32,
            1.41,
            1.45,
            1.49
    };

    public ConsistencyDTO calculateConsistency(
            double[][] matrix,
            double[] weights
    ) {

        int n = matrix.length;

        double[] weightedSum =
                new double[n];

        for (int i = 0; i < n; i++) {

            double sum = 0.0;

            for (int j = 0; j < n; j++) {

                sum += matrix[i][j] * weights[j];
            }

            weightedSum[i] = sum;
        }

        double lambdaMax = 0.0;

        for (int i = 0; i < n; i++) {

            lambdaMax +=
                    weightedSum[i] / weights[i];
        }

        lambdaMax /= n;

        double ci =
                (lambdaMax - n) / (n - 1);

        double ri =
                RANDOM_INDEX[n - 1];

        double cr;

        if (ri == 0) {

            cr = 0;

        } else {

            cr = ci / ri;
        }

        return new ConsistencyDTO(
                round(ci),
                round(cr),
                cr < 0.10
        );
    }
}