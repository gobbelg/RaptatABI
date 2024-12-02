/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer.algorithms;

import java.util.Arrays;

/** @author sahask */
public class HungarianAlgorithm {

  private final int[][] costMatrix;

  private final int rows, cols, dim;
  private final int[] labelByWorker, labelByJob;

  private final int[] minSlackWorkerByJob;

  private final int[] minSlackValueByJob;
  private final int[] matchJobByWorker, matchWorkerByJob;
  private final int[] parentWorkerByCommittedJob;
  private final boolean[] committedWorkers;

  /**
   * Construct an instance of the algorithm.
   *
   * @param costMatrix the cost matrix, where matrix[i][j] holds the cost of assigning worker i to
   *        job j, for all i, j. The cost matrix must not be irregular in the sense that all rows
   *        must be the same length.
   */
  public HungarianAlgorithm(int[][] costMatrix) {
    this.dim = Math.max(costMatrix.length, costMatrix[0].length);
    this.rows = costMatrix.length;
    this.cols = costMatrix[0].length;
    this.costMatrix = new int[this.dim][this.dim];

    for (int w = 0; w < this.dim; w++) {
      if (w < costMatrix.length) {
        if (costMatrix[w].length != this.cols) {
          throw new IllegalArgumentException("Irregular cost matrix");
        }
        this.costMatrix[w] = Arrays.copyOf(costMatrix[w], this.dim);
      } else {
        this.costMatrix[w] = new int[this.dim];
      }
    }

    this.labelByWorker = new int[this.dim];
    this.labelByJob = new int[this.dim];
    this.minSlackWorkerByJob = new int[this.dim];
    this.minSlackValueByJob = new int[this.dim];
    this.committedWorkers = new boolean[this.dim];
    this.parentWorkerByCommittedJob = new int[this.dim];
    this.matchJobByWorker = new int[this.dim];
    Arrays.fill(this.matchJobByWorker, -1);
    this.matchWorkerByJob = new int[this.dim];
    Arrays.fill(this.matchWorkerByJob, -1);
  }


  public HungarianAlgorithm(int[][] costMatrix, boolean minimizeCost) {
    if (!minimizeCost) {
      int maxArrayValue = maxArrayValue(costMatrix);
      costMatrix = diffArray(maxArrayValue, costMatrix);
    }

    // If the raptat annotation size is 0
    // then costMatrix[0] is null
    if (costMatrix.length == 0) {
      this.dim = 0;
      this.rows = 0;
      this.cols = 0;
    } else {
      this.dim = Math.max(costMatrix.length, costMatrix[0].length);
      this.rows = costMatrix.length;
      this.cols = costMatrix[0].length;
    }

    this.costMatrix = new int[this.dim][this.dim];

    for (int w = 0; w < this.dim; w++) {
      if (w < costMatrix.length) {
        if (costMatrix[w].length != this.cols) {
          throw new IllegalArgumentException("Irregular cost matrix");
        }
        this.costMatrix[w] = Arrays.copyOf(costMatrix[w], this.dim);
        for (int i = costMatrix[w].length; i < this.costMatrix[w].length; i++) {
          this.costMatrix[w][i] = Integer.MAX_VALUE;
        }
      } else {
        this.costMatrix[w] = new int[this.dim];
        Arrays.fill(this.costMatrix[w], Integer.MAX_VALUE);
      }
    }

    this.labelByWorker = new int[this.dim];
    this.labelByJob = new int[this.dim];
    this.minSlackWorkerByJob = new int[this.dim];
    this.minSlackValueByJob = new int[this.dim];
    this.committedWorkers = new boolean[this.dim];
    this.parentWorkerByCommittedJob = new int[this.dim];
    this.matchJobByWorker = new int[this.dim];
    Arrays.fill(this.matchJobByWorker, -1);
    this.matchWorkerByJob = new int[this.dim];
    Arrays.fill(this.matchWorkerByJob, -1);
  }


  /**
   * Execute the algorithm.
   *
   * @return The minimum cost matching of workers to jobs based upon the provided cost matrix. A
   *         matching value of -1 indicates that the corresponding worker is unassigned.<br>
   *         <br>
   *         Note that the numbers in the return array correspond to the column value for each
   *         corresponding row that maximizes the matching. For example, if return = [ 2 1 3 0], it
   *         indicates that the maximum matching is provided by taking column 2 for row 0, column 1
   *         for row 1, column 3 for row 2, and column 0 for row 3
   */
  public int[] execute() {
    /*
     * Heuristics to improve performance: Reduce rows and columns by their smallest element, compute
     * an initial non-zero dual feasible solution and create a greedy matching from workers to jobs
     * of the cost matrix.
     */
    reduce();
    computeInitialFeasibleSolution();
    greedyMatch();

    int w = fetchUnmatchedWorker();

    while (w < this.dim) {
      initializePhase(w);
      executePhase();
      w = fetchUnmatchedWorker();
    }

    int[] result = Arrays.copyOf(this.matchJobByWorker, this.rows);

    for (w = 0; w < result.length; w++) {
      if (result[w] >= this.cols) {
        result[w] = -1;
      }
    }

    return result;
  }


  public void getOptimalSolution() {
    int[] result = execute();
    displayMatrix(this.costMatrix);

    System.out.println("\nResult:\n" + arrayToDelimString(result, "\t"));
    System.out.println("Costs:");

    int runningTotal = 0;

    for (int j = 0; j < this.costMatrix.length; j++) {
      runningTotal += this.costMatrix[j][result[j]];
      System.out.print(this.costMatrix[j][result[j]] + "\t");
    }

    System.out.println("\nTotal Cost:\n" + runningTotal);
    System.out.print("\n\n");
  }


  /**
   * Compute an initial feasible solution by assigning zero labels to the workers and by assigning
   * to each job a label equal to the minimum cost among its incident edges.
   */
  protected void computeInitialFeasibleSolution() {
    for (int j = 0; j < this.dim; j++) {
      this.labelByJob[j] = Integer.MAX_VALUE;
    }

    for (int w = 0; w < this.dim; w++) {
      for (int j = 0; j < this.dim; j++) {
        if (this.costMatrix[w][j] < this.labelByJob[j]) {
          this.labelByJob[j] = this.costMatrix[w][j];
        }
      }
    }
  }


  /**
   * Execute a single phase of the algorithm. A phase of the Hungarian algorithm consists of
   * building a set of committed workers and a set of committed jobs from a root unmatched worker by
   * following alternating unmatched/matched zero-slack edges. If an unmatched job is encountered,
   * then an augmenting path has been found and the matching is grown. If the connected zero-slack
   * edges have been exhausted, the labels of committed workers are increased by the minimum slack
   * among committed workers and non-committed jobs to create more zero-slack edges (the labels of
   * committed jobs are simultaneously decreased by the same amount in order to maintain a feasible
   * labeling).
   *
   * <p>
   * The runtime of a single phase of the algorithm is O(n^2), where n is the dimension of the
   * internal square cost matrix, since each edge is visited at most once and since increasing the
   * labeling is accomplished in time O(n) by maintaining the minimum slack values among
   * non-committed jobs. When a phase completes, the matching will have increased in size.
   */
  protected void executePhase() {
    while (true) {
      int minSlackWorker = -1, minSlackJob = -1;
      double minSlackValue = Double.POSITIVE_INFINITY;

      for (int j = 0; j < this.dim; j++) {
        if (this.parentWorkerByCommittedJob[j] == -1) {
          if (this.minSlackValueByJob[j] < minSlackValue) {
            minSlackValue = this.minSlackValueByJob[j];
            minSlackWorker = this.minSlackWorkerByJob[j];
            minSlackJob = j;
          }
        }
      }

      if (minSlackValue > 0) {
        updateLabeling(minSlackValue);
      }

      this.parentWorkerByCommittedJob[minSlackJob] = minSlackWorker;

      if (this.matchWorkerByJob[minSlackJob] == -1) {
        /*
         * An augmenting path has been found.
         */
        int committedJob = minSlackJob;
        int parentWorker = this.parentWorkerByCommittedJob[committedJob];

        while (true) {
          int temp = this.matchJobByWorker[parentWorker];
          match(parentWorker, committedJob);
          committedJob = temp;

          if (committedJob == -1) {
            break;
          }
          parentWorker = this.parentWorkerByCommittedJob[committedJob];
        }
        return;
      } else {
        /*
         * Update slack values since we increased the size of the committed workers set.
         */
        int worker = this.matchWorkerByJob[minSlackJob];
        this.committedWorkers[worker] = true;

        for (int j = 0; j < this.dim; j++) {
          if (this.parentWorkerByCommittedJob[j] == -1) {
            int slack =
                this.costMatrix[worker][j] - this.labelByWorker[worker] - this.labelByJob[j];

            if (this.minSlackValueByJob[j] > slack) {
              this.minSlackValueByJob[j] = slack;
              this.minSlackWorkerByJob[j] = worker;
            }
          }
        }
      }
    }
  }


  /** @return the first unmatched worker or {@link #dim} if none. */
  protected int fetchUnmatchedWorker() {
    int w;

    for (w = 0; w < this.dim; w++) {
      if (this.matchJobByWorker[w] == -1) {
        break;
      }
    }

    return w;
  }


  /**
   * Find a valid matching by greedily selecting among zero-cost matchings. This is a heuristic to
   * jump-start the augmentation algorithm.
   */
  protected void greedyMatch() {
    for (int w = 0; w < this.dim; w++) {
      for (int j = 0; j < this.dim; j++) {
        if (this.matchJobByWorker[w] == -1 && this.matchWorkerByJob[j] == -1
            && this.costMatrix[w][j] - this.labelByWorker[w] - this.labelByJob[j] == 0) {
          match(w, j);
        }
      }
    }
  }


  /**
   * Initialize the next phase of the algorithm by clearing the committed workers and jobs sets and
   * by initializing the slack arrays to the values corresponding to the specified root worker.
   *
   * @param w the worker at which to root the next phase.
   */
  protected void initializePhase(int w) {
    Arrays.fill(this.committedWorkers, false);
    Arrays.fill(this.parentWorkerByCommittedJob, -1);
    this.committedWorkers[w] = true;

    for (int j = 0; j < this.dim; j++) {
      this.minSlackValueByJob[j] =
          this.costMatrix[w][j] - this.labelByWorker[w] - this.labelByJob[j];
      this.minSlackWorkerByJob[j] = w;
    }
  }


  /**
   * Helper method to record a matching between worker w and job j.
   *
   * @param w
   * @param j
   */
  protected void match(int w, int j) {
    this.matchJobByWorker[w] = j;
    this.matchWorkerByJob[j] = w;
  }


  /**
   * Reduce the cost matrix by subtracting the smallest element of each row from all elements of the
   * row as well as the smallest element of each column from all elements of the column. Note that
   * an optimal assignment for a reduced cost matrix is optimal for the original cost matrix.
   */
  protected void reduce() {
    for (int w = 0; w < this.dim; w++) {
      double min = Double.POSITIVE_INFINITY;

      for (int j = 0; j < this.dim; j++) {
        if (this.costMatrix[w][j] < min) {
          min = this.costMatrix[w][j];
        }
      }

      for (int j = 0; j < this.dim; j++) {
        this.costMatrix[w][j] -= min;
      }
    }

    int[] min = new int[this.dim];

    for (int j = 0; j < this.dim; j++) {
      min[j] = Integer.MAX_VALUE;
    }

    for (int w = 0; w < this.dim; w++) {
      for (int j = 0; j < this.dim; j++) {
        if (this.costMatrix[w][j] < min[j]) {
          min[j] = this.costMatrix[w][j];
        }
      }
    }

    for (int w = 0; w < this.dim; w++) {
      for (int j = 0; j < this.dim; j++) {
        this.costMatrix[w][j] -= min[j];
      }
    }
  }


  // public static void main(String args[]) {
  // int dim = 10;
  // int trials = 5;
  // int maxCost = 10 * dim;
  //
  // int[][][] cost = createCostArraySamples(dim, trials, maxCost);
  //
  // for (int i = 0; i < cost.length; i++) {
  // System.out.println("---------\nTrial " + (i + 1) + ":");
  //
  // boolean minimizeCost = true;
  // System.out.println("MINIMIZE ALGORITHM");
  // findOptimalSolution(cost[i], minimizeCost);
  //
  // System.out.println("MAXIMIZE ALGORITHM");
  // findOptimalSolution(cost[i], !minimizeCost);
  // }
  // }

  /**
   * Update labels with the specified slack by adding the slack value for committed workers and by
   * subtracting the slack value for committed jobs. In addition, update the minimum slack values
   * appropriately.
   *
   * @param slack
   */
  protected void updateLabeling(double slack) {
    for (int w = 0; w < this.dim; w++) {
      if (this.committedWorkers[w]) {
        this.labelByWorker[w] += slack;
      }
    }

    for (int j = 0; j < this.dim; j++) {
      if (this.parentWorkerByCommittedJob[j] != -1) {
        this.labelByJob[j] -= slack;
      } else {
        this.minSlackValueByJob[j] -= slack;
      }
    }
  }


  /**
   * Create a single string from the array of attributes and return it with the delimiter between
   * the strings
   */
  private String arrayToDelimString(int[] inArray, String delimiter) {
    StringBuilder resultSB = new StringBuilder(String.valueOf(inArray[0]));

    for (int i = 1; i < inArray.length; i++) {
      resultSB.append(delimiter).append(String.valueOf(inArray[i]));
    }

    return resultSB.toString();
    //
    // String resultString = new String(resultSB);
    // return resultString;
  }


  private int[][] diffArray(int diffValue, int[][] inputArray) {
    int[][] resultArray = new int[inputArray.length][];

    for (int i = 0; i < inputArray.length; i++) {
      resultArray[i] = new int[inputArray[i].length];
      for (int j = 0; j < inputArray[i].length; j++) {
        long difference = (long) diffValue - (long) inputArray[i][j];
        resultArray[i][j] = (int) (difference > Integer.MAX_VALUE ? Integer.MAX_VALUE : difference);
      }
    }

    return resultArray;
  }


  private void displayMatrix(int[][] inMatrix) {
    for (int[] inMatrix1 : inMatrix) {
      System.out.println(arrayToDelimString(inMatrix1, "\t"));
    }
  }


  private int maxArrayValue(int[][] inputArray) {
    int result = Integer.MIN_VALUE;

    for (int[] inputArray1 : inputArray) {
      for (int j = 0; j < inputArray1.length; j++) {
        result = Math.max(result, inputArray1[j]);
      }
    }

    return result;
  }


  public static void main(String[] args) {
    int[][] testMatrix = new int[0][0];
    HungarianAlgorithm ha = new HungarianAlgorithm(testMatrix, false);

    int[] results = ha.execute();

    System.out.println("Finished:" + results);
  }
}
