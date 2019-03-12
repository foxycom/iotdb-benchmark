package cn.edu.tsinghua.iotdb.benchmark.db.leveldb;

import cn.edu.tsinghua.iotdb.benchmark.conf.Config;
import cn.edu.tsinghua.iotdb.benchmark.conf.ConfigDescriptor;
import edu.tsinghua.k1.api.ITimeSeriesDB;
import edu.tsinghua.k1.leveldb.LevelTimeSeriesDBFactory;
import java.io.File;
import java.io.IOException;
import org.iq80.leveldb.Options;

public class LevelDB {
  private Options options;

  public ITimeSeriesDB getTimeSeriesDB() {
    return timeSeriesDB;
  }

  private ITimeSeriesDB timeSeriesDB = null;
  private static Config config  = ConfigDescriptor.getInstance().getConfig();

  private static class LevelDBHolder{
    private static final LevelDB INSTANCE = new LevelDB();
  }

  public static LevelDB getInstance(){
    return LevelDBHolder.INSTANCE;
  }

  LevelDB(){
    File file = new File(config.GEN_DATA_FILE_PATH);
    System.out.println("creating timeSeriesDB...");
    options = new Options();
    options.createIfMissing(true);
    // 128M
    options.writeBufferSize(128<<20);
    // none read cache
    options.maxOpenFiles(1<<20);
    try {
      timeSeriesDB = LevelTimeSeriesDBFactory.getInstance().openOrCreate(file, options);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      // if the is not null, close it
    }
  }

}
