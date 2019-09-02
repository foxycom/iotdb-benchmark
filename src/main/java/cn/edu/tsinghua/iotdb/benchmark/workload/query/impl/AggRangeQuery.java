package cn.edu.tsinghua.iotdb.benchmark.workload.query.impl;

import cn.edu.tsinghua.iotdb.benchmark.enums.Aggregation;
import cn.edu.tsinghua.iotdb.benchmark.workload.schema.DeviceSchema;
import cn.edu.tsinghua.iotdb.benchmark.workload.schema.SensorGroup;

import java.util.List;

public class AggRangeQuery extends RangeQuery {

  private Aggregation aggrFunc;

  public AggRangeQuery(List<DeviceSchema> deviceSchema, SensorGroup sensorGroup, long startTimestamp,
                       long endTimestamp, Aggregation aggrFunc) {
    super(deviceSchema, sensorGroup, startTimestamp, endTimestamp);
    this.aggrFunc = aggrFunc;
  }

  public AggRangeQuery(List<DeviceSchema> deviceSchema, long startTimestamp, long endTimestamp, Aggregation aggrFunc) {
    super(deviceSchema, startTimestamp, endTimestamp);
    this.aggrFunc = aggrFunc;
  }

  public Aggregation getAggrFunc() {
    return aggrFunc;
  }
}
