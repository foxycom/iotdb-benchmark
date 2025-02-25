package de.uni_passau.dbts.benchmark.distribution;

import de.uni_passau.dbts.benchmark.conf.Config;
import de.uni_passau.dbts.benchmark.conf.ConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class PossionDistribution {
  private Config config;
  private double lambda;
  private Random random;
  private int deltaKinds;
  private final double basicModelLambda = 10;
  private final int basicModelMaxK = 25;

  public PossionDistribution(Random ran) {
    this.config = ConfigParser.INSTANCE.config();
    this.random = ran;
    this.lambda = config.LAMBDA;
    this.deltaKinds = config.MAX_K;
  }

  private double getPossionProbability(int k, double la) {
    double c = Math.exp(-la), sum = 1;
    for (int i = 1; i <= k; i++) {
      sum *= la / i;
    }
    return sum * c;
  }

  public int getNextPossionDelta() {
    int nextDelta = 0;
    if (lambda < 500) {
      double rand = random.nextDouble();
      double[] p = new double[config.MAX_K];
      double sum = 0;
      for (int i = 0; i < config.MAX_K - 1; i++) {
        p[i] = getPossionProbability(i, config.LAMBDA);
        sum += p[i];
      }
      p[config.MAX_K - 1] = 1 - sum;
      double[] range = new double[config.MAX_K + 1];
      range[0] = 0;
      for (int i = 0; i < config.MAX_K; i++) {
        range[i + 1] = range[i] + p[i];
      }
      for (int i = 0; i < config.MAX_K; i++) {
        nextDelta++;
        if (isBetween(rand, range[i], range[i + 1])) {
          break;
        }
      }
    } else {
      double rand = random.nextDouble();
      double[] p = new double[basicModelMaxK];
      double sum = 0;
      for (int i = 0; i < basicModelMaxK - 1; i++) {
        p[i] = getPossionProbability(i, basicModelLambda);
        sum += p[i];
      }
      p[basicModelMaxK - 1] = 1 - sum;
      double[] range = new double[basicModelMaxK + 1];
      range[0] = 0;
      for (int i = 0; i < basicModelMaxK; i++) {
        range[i + 1] = range[i] + p[i];
      }
      for (int i = 0; i < basicModelMaxK; i++) {
        nextDelta++;
        if (isBetween(rand, range[i], range[i + 1])) {
          break;
        }
      }
      double step;
      if (nextDelta <= basicModelLambda) {
        step = lambda / basicModelLambda;
      } else {
        step = (deltaKinds - lambda) / (basicModelMaxK - basicModelLambda);
      }
      nextDelta = (int) (lambda + ((nextDelta - basicModelLambda) * step));
    }
    return nextDelta;
  }

  private static boolean isBetween(double a, double b, double c) {
    return a > b && a < c;
  }
}
