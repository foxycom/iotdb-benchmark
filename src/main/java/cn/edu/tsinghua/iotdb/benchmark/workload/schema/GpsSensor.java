package cn.edu.tsinghua.iotdb.benchmark.workload.schema;

import cn.edu.tsinghua.iotdb.benchmark.conf.Constants;
import cn.edu.tsinghua.iotdb.benchmark.function.GeoFunction;
import cn.edu.tsinghua.iotdb.benchmark.tsdb.DB;

public class GpsSensor extends BasicSensor {
    private GeoFunction function;

    public GpsSensor(String name, SensorGroup sensorGroup, GeoFunction function, int freq, String dataType) {
        super(name, sensorGroup, null, freq, dataType);
        this.function = function;
    }

    @Override
    public String getValue(long currentTimestamp, DB currentDb) {
        GeoPoint location = function.get(Constants.SPAWN_POINT);
        return location.getValue(currentDb);
    }
}
