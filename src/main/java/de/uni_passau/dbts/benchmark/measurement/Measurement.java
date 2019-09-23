package de.uni_passau.dbts.benchmark.measurement;

import de.uni_passau.dbts.benchmark.client.OperationController.Operation;
import de.uni_passau.dbts.benchmark.conf.Config;
import de.uni_passau.dbts.benchmark.conf.ConfigParser;

import java.util.*;

import de.uni_passau.dbts.benchmark.mysql.MySqlLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Measurement {

  private static final Logger LOGGER = LoggerFactory.getLogger(Measurement.class);
  private static Config config = ConfigParser.INSTANCE.config();
  private static final double[] MID_AVG_RANGE = {0.1, 0.9};
  private Map<Operation, List<Double>> operationLatencies;

  /* The sums of latencies per operation per thread. */
  private Map<Operation, List<Double>> getOperationLatencySumsList;
  private Map<Operation, Double> operationLatencySums;
  private double createSchemaTime;
  private int loopIndex = -1;
  private String remark;

  private MySqlLog mysql;

  /* Total measurement time including schema registration and batch generation. */
  private double elapseTime;

  private Map<Operation, Long> okOperationNumMap;
  private Map<Operation, Long> failOperationNumMap;
  private Map<Operation, Long> okPointNumMap;
  private Map<Operation, Long> failPointNumMap;

  public Measurement() {
    mysql = new MySqlLog(config.MYSQL_INIT_TIMESTAMP);
    mysql.initMysql(false);

    operationLatencies = new EnumMap<>(Operation.class);
    for (Operation operation : Operation.values()) {
      operationLatencies.put(operation, new ArrayList<>());
    }
    okOperationNumMap = new EnumMap<>(Operation.class);
    for (Operation operation : Operation.values()) {
      okOperationNumMap.put(operation, 0L);
    }
    failOperationNumMap = new EnumMap<>(Operation.class);
    for (Operation operation : Operation.values()) {
      failOperationNumMap.put(operation, 0L);
    }
    okPointNumMap = new EnumMap<>(Operation.class);
    for (Operation operation : Operation.values()) {
      okPointNumMap.put(operation, 0L);
    }
    failPointNumMap = new EnumMap<>(Operation.class);
    for (Operation operation : Operation.values()) {
      failPointNumMap.put(operation, 0L);
    }
    operationLatencySums = new EnumMap<>(Operation.class);
    for (Operation operation : Operation.values()) {
      operationLatencySums.put(operation, 0D);
    }
    getOperationLatencySumsList = new EnumMap<>(Operation.class);
    for (Operation operation : Operation.values()) {
      getOperationLatencySumsList.put(operation, new ArrayList<>());
    }
  }

  private Map<Operation, Double> getOperationLatencySums() {
    for (Operation operation : Operation.values()) {
      operationLatencySums.put(operation, getDoubleListSum(operationLatencies.get(operation)));
    }
    return operationLatencySums;
  }

  public long getOkOperationNum(Operation operation) {
    return okOperationNumMap.get(operation);
  }

  public long getFailOperationNum(Operation operation) {
    return failOperationNumMap.get(operation);
  }

  public long getOkPointNum(Operation operation) {
    return okPointNumMap.get(operation);
  }

  public long getFailPointNum(Operation operation) {
    return failPointNumMap.get(operation);
  }

  public void addOperationLatency(Operation op, double latency) {
    operationLatencies.get(op).add(latency);
  }

  public void addOkPointNum(Operation operation, int pointNum) {
    okPointNumMap.put(operation, okPointNumMap.get(operation) + pointNum);
  }

  public void addFailPointNum(Operation operation, int pointNum) {
    failPointNumMap.put(operation, failPointNumMap.get(operation) + pointNum);
  }

  public void addOkOperation(Operation operation) {
    okOperationNumMap.put(operation, okOperationNumMap.get(operation) + 1);
  }

  public void addFailOperation(Operation operation) {
    failOperationNumMap.put(operation, failOperationNumMap.get(operation) + 1);
  }

  public void incrementLoopIndex() {
    loopIndex++;
  }

  private Map<Operation, List<Double>> getOperationLatencies() {
    return operationLatencies;
  }

  public void addOkOperation(Operation operation, double latency, long okPointNum) {
    okOperationNumMap.put(operation, okOperationNumMap.get(operation) + 1);
    operationLatencies.get(operation).add(latency);
    okPointNumMap.put(operation, okPointNumMap.get(operation) + okPointNum);

    double rate = okPointNum / (latency / 1000); // Points/s
    flushOk(operation.getName(), latency, rate, okPointNum);
  }

  public void addFailOperation(Operation operation, long failPointNum) {
    failOperationNumMap.put(operation, failOperationNumMap.get(operation) + 1);
    failPointNumMap.put(operation, failPointNumMap.get(operation) + failPointNum);

    flushFail(operation.getName(), failPointNum);
  }

  private void flushOk(String operation, double latency, double rate, long okPointNum) {
    String clientName = Thread.currentThread().getName();
    mysql.saveClientMeasurement(
        clientName, operation, "OK", loopIndex, latency, elapseTime, rate, okPointNum, 0, remark);
  }

  private void flushFail(String operation, long failPointNum) {
    String clientName = Thread.currentThread().getName();
    mysql.saveClientMeasurement(
        clientName, operation, "FAILED", loopIndex, elapseTime, 0, 0, 0, failPointNum, remark);
  }

  public void save() {

    for (Operation operation : Operation.values()) {
      if (okOperationNumMap.get(operation) == 0 && failOperationNumMap.get(operation) == 0) {
        continue;
      }

      double accTime = Metric.MAX_THREAD_LATENCY_SUM.getTypeValueMap().get(operation);
      double accRate = 0;
      if (accTime != 0) {
        accRate = okPointNumMap.get(operation) * 1000 / accTime;
      }

      mysql.saveOverallMeasurement(
          operation.getName(),
          Metric.AVG_LATENCY.getTypeValueMap().get(operation),
          Metric.P99_LATENCY.getTypeValueMap().get(operation),
          Metric.MEDIAN_LATENCY.getTypeValueMap().get(operation),
          accTime,
          accRate,
          okPointNumMap.get(operation),
          failPointNumMap.get(operation),
          remark);
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public void setOperationLatencies(Map<Operation, List<Double>> operationLatencies) {
    this.operationLatencies = operationLatencies;
  }

  public double getCreateSchemaTime() {
    return createSchemaTime;
  }

  public void setCreateSchemaTime(double createSchemaTime) {
    this.createSchemaTime = createSchemaTime;
  }

  public double getElapseTime() {
    return elapseTime;
  }

  public void setElapseTime(double elapseTime) {
    this.elapseTime = elapseTime;
  }

  /**
   * users need to call calculateMetrics() after calling mergeMeasurement() to update metrics.
   *
   * @param m measurement to be merged
   */
  public void mergeMeasurement(Measurement m) {
    for (Operation operation : Operation.values()) {
      operationLatencies.get(operation).addAll(m.getOperationLatencies().get(operation));
      getOperationLatencySumsList.get(operation).add(m.getOperationLatencySums().get(operation));
      okOperationNumMap.put(
          operation, okOperationNumMap.get(operation) + m.getOkOperationNum(operation));
      failOperationNumMap.put(
          operation, failOperationNumMap.get(operation) + m.getFailOperationNum(operation));
      okPointNumMap.put(operation, okPointNumMap.get(operation) + m.getOkPointNum(operation));
      failPointNumMap.put(operation, failPointNumMap.get(operation) + m.getFailPointNum(operation));
    }
  }

  public void calculateMetrics() {
    for (Operation operation : Operation.values()) {
      List<Double> latencyList = operationLatencies.get(operation);
      if (!latencyList.isEmpty()) {
        int totalOps = latencyList.size();
        double sumLatency = 0;
        for (double latency : latencyList) {
          sumLatency += latency;
        }
        double avgLatency = sumLatency / totalOps;

        // Time elapsed since the start and the time the last thread completed the operation.
        double maxThreadLatencySum = getDoubleListMax(getOperationLatencySumsList.get(operation));

        Metric.MAX_THREAD_LATENCY_SUM.getTypeValueMap().put(operation, maxThreadLatencySum);
        Metric.AVG_LATENCY.getTypeValueMap().put(operation, avgLatency);
        latencyList.sort(new DoubleComparator());
        Metric.MIN_LATENCY.getTypeValueMap().put(operation, latencyList.get(0));
        Metric.MAX_LATENCY.getTypeValueMap().put(operation, latencyList.get(totalOps - 1));
        Metric.P10_LATENCY
            .getTypeValueMap()
            .put(operation, latencyList.get((int) (totalOps * 0.10)));
        Metric.P25_LATENCY
            .getTypeValueMap()
            .put(operation, latencyList.get((int) (totalOps * 0.25)));
        Metric.MEDIAN_LATENCY
            .getTypeValueMap()
            .put(operation, latencyList.get((int) (totalOps * 0.50)));
        Metric.P75_LATENCY
            .getTypeValueMap()
            .put(operation, latencyList.get((int) (totalOps * 0.75)));
        Metric.P90_LATENCY
            .getTypeValueMap()
            .put(operation, latencyList.get((int) (totalOps * 0.90)));
        Metric.P95_LATENCY
            .getTypeValueMap()
            .put(operation, latencyList.get((int) (totalOps * 0.95)));
        Metric.P99_LATENCY
            .getTypeValueMap()
            .put(operation, latencyList.get((int) (totalOps * 0.99)));
        double midAvgLatency = 0;
        double midSum = 0;
        int midCount = 0;
        for (int i = (int) (totalOps * MID_AVG_RANGE[0]);
            i < (int) (totalOps * MID_AVG_RANGE[1]);
            i++) {
          midSum += latencyList.get(i);
          midCount++;
        }
        if (midCount != 0) {
          midAvgLatency = midSum / midCount;
        } else {
          LOGGER.error(
              "Can not calculate mid-average latency because mid-operation number is zero.");
        }
        Metric.MID_AVG_LATENCY.getTypeValueMap().put(operation, midAvgLatency);
      }
    }
  }

  public void showMeasurements() {
    System.out.println(Thread.currentThread().getName() + " measurements:");
    System.out.println("Test elapse time: " + String.format("%.2f", elapseTime) + " second");
    System.out.println("Create schema cost " + String.format("%.2f", createSchemaTime) + " second");

    System.out.println(
        "--------------------------------------------------Result Matrix--------------------------------------------------");
    String intervalString = "\t\t";
    System.out.println(
        "Operation\t\tokOperation\tokPoint\t\tfailOperation\tfailPoint\telapseRate\taccTime\taccRate");
    for (Operation operation : Operation.values()) {
      System.out.print(operation.getName() + intervalString);
      System.out.print(okOperationNumMap.get(operation) + intervalString);
      System.out.print(okPointNumMap.get(operation) + intervalString);
      System.out.print(failOperationNumMap.get(operation) + intervalString);
      System.out.print(failPointNumMap.get(operation) + intervalString);
      double accTime = Metric.MAX_THREAD_LATENCY_SUM.typeValueMap.get(operation) / 1000;
      String elapseRate = String.format("%.2f", okPointNumMap.get(operation) / elapseTime);
      double accRate = 0;
      if (accTime != 0) {
        accRate = okPointNumMap.get(operation) / accTime;
      }
      String rate = String.format("%.2f", accRate);
      System.out.print(elapseRate + intervalString);

      String time = String.format("%.2f", accTime);
      System.out.print(time + intervalString);
      System.out.println(rate + intervalString);
    }
    System.out.println(
        "-----------------------------------------------------------------------------------------------------------------");
  }

  public void showConfigs() {
    System.out.println("----------------------Test Configurations----------------------");
    System.out.println("DB_SWITCH: " + config.DB_SWITCH);
    System.out.println("OPERATION_PROPORTION: " + config.OPERATION_PROPORTION);
    System.out.println("IS_CLIENT_BIND: " + config.BIND_CLIENTS_TO_DEVICES);
    System.out.println("CLIENT_NUMBER: " + config.CLIENTS_NUMBER);
    System.out.println("GROUP_NUMBER: " + config.DEVICE_GROUPS_NUMBER);
    System.out.println("DEVICE_NUMBER: " + config.DEVICES_NUMBER);
    System.out.println("SENSOR_NUMBER: " + config.SENSORS_NUMBER);
    System.out.println("BATCH_SIZE: " + config.BATCH_SIZE);
    System.out.println("LOOP: " + config.LOOP);
    System.out.println("QUERY_INTERVAL: " + config.QUERY_INTERVAL);
    System.out.println("IS_OVERFLOW: " + config.USE_OVERFLOW);
    System.out.println("OVERFLOW_MODE: " + config.OVERFLOW_MODE);
    System.out.println("OVERFLOW_RATIO: " + config.OVERFLOW_RATIO);
    System.out.println("---------------------------------------------------------------");
  }

  public void showMetrics() {
    System.out.println(
        "-----------------------------------------------Latency (ms) Matrix-----------------------------------------------");
    String intervalString = "\t";
    System.out.print("Operation" + intervalString);
    for (Metric metric : Metric.values()) {
      System.out.print(metric.name + intervalString);
    }
    System.out.println();
    for (Operation operation : Operation.values()) {
      System.out.print(operation.getName() + intervalString);
      for (Metric metric : Metric.values()) {
        System.out.print(
            String.format("%.2f", metric.typeValueMap.get(operation)) + intervalString);
      }
      System.out.println();
    }
    System.out.println(
        "-----------------------------------------------------------------------------------------------------------------");
  }

  class DoubleComparator implements Comparator<Double> {

    @Override
    public int compare(Double a, Double b) {
      if (a < b) {
        return -1;
      } else if (Objects.equals(a, b)) {
        return 0;
      } else {
        return 1;
      }
    }
  }

  private double getDoubleListSum(List<Double> list) {
    double sum = 0;
    for (double item : list) {
      sum += item;
    }
    return sum;
  }

  private double getDoubleListMax(List<Double> list) {
    double max = 0;
    for (double item : list) {
      max = Math.max(max, item);
    }
    return max;
  }

  public enum Metric {
    AVG_LATENCY("AVG"),
    MID_AVG_LATENCY("MID_AVG"),
    MIN_LATENCY("MIN"),
    P10_LATENCY("P10"),
    P25_LATENCY("P25"),
    MEDIAN_LATENCY("MEDIAN"),
    P75_LATENCY("P75"),
    P90_LATENCY("P90"),
    P95_LATENCY("P95"),
    P99_LATENCY("P99"),
    MAX_LATENCY("MAX"),
    MAX_THREAD_LATENCY_SUM("MAX_SUM");

    public Map<Operation, Double> getTypeValueMap() {
      return typeValueMap;
    }

    Map<Operation, Double> typeValueMap;

    public String getName() {
      return name;
    }

    String name;

    Metric(String name) {
      this.name = name;
      typeValueMap = new EnumMap<>(Operation.class);
      for (Operation operation : Operation.values()) {
        typeValueMap.put(operation, 0D);
      }
    }
  }
}
