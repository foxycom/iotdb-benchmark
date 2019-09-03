/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package cn.edu.tsinghua.iotdb.benchmark.tsdb.fakedb;

import cn.edu.tsinghua.iotdb.benchmark.measurement.Status;
import cn.edu.tsinghua.iotdb.benchmark.tsdb.IDatabase;
import cn.edu.tsinghua.iotdb.benchmark.tsdb.TsdbException;
import cn.edu.tsinghua.iotdb.benchmark.workload.ingestion.Batch;
import cn.edu.tsinghua.iotdb.benchmark.workload.query.impl.*;
import cn.edu.tsinghua.iotdb.benchmark.workload.schema.DeviceSchema;
import java.util.List;

public class FakeDB implements IDatabase {

  @Override
  public void init() throws TsdbException {

  }

  @Override
  public void cleanup() throws TsdbException {

  }

  @Override
  public void close() throws TsdbException {

  }

  @Override
  public void registerSchema(List<DeviceSchema> schemaList) throws TsdbException {

  }

  @Override
  public float getSize() throws TsdbException {
    return 0;
  }

  @Override
  public Status insertOneBatch(Batch batch) {
    return new Status(true, 1000000L);
  }

  @Override
  public Status preciseQuery(PreciseQuery preciseQuery) {
    return new Status(true, 1000000L, 0);
  }

  @Override
  public Status rangeQuery(RangeQuery rangeQuery) {
    return new Status(true, 1000000L, 0);
  }

  @Override
  public Status gpsRangeQuery(RangeQuery RangeQuery) {
    return null;
  }

  @Override
  public Status gpsValueRangeQuery(GpsRangeQuery rangeQuery) {
    return null;
  }

  @Override
  public Status valueRangeQuery(ValueRangeQuery valueRangeQuery) {
    return new Status(true, 1000000L, 0);
  }

  @Override
  public Status aggRangeQuery(AggRangeQuery aggRangeQuery) {
    return new Status(true, 1000000L, 0);
  }

  @Override
  public Status aggValueQuery(AggValueQuery aggValueQuery) {
    return new Status(true, 1000000L, 0);
  }

  @Override
  public Status aggRangeValueQuery(AggRangeValueQuery aggRangeValueQuery) {
    return new Status(true, 1000000L, 0);
  }

  @Override
  public Status groupByQuery(GroupByQuery groupByQuery) {
    return new Status(true, 1000000L, 0);
  }

  @Override
  public Status latestPointQuery(LatestPointQuery latestPointQuery) {
    return new Status(true, 1000000L, 0);
  }

  @Override
  public Status heatmapRangeQuery(GpsRangeQuery gpsRangeQuery) {
    return null;
  }

  @Override
  public Status distanceRangeQuery(GpsRangeQuery gpsRangeQuery) {
    return null;
  }

  @Override
  public Status bikesInLocationQuery(GpsRangeQuery gpsRangeQuery) {
    return null;
  }
}
