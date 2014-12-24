import java.util.ArrayList;

/**
 * A class that contains all the balls in the game and is responsible for
 * calculating collisions between the balls.
 * 
 * @author Guohong Yang
 *
 */
public class BallFactory {

	private ArrayList<Ball> balls;

	public BallFactory() {
		balls = new ArrayList<Ball>();
	}

	public ArrayList<Ball> getBalls() {
		return balls;
	}

	public void add(Ball ball) {
		balls.add(ball);
	}

	public boolean moveBalls() {
		boolean isRunning = false;
		for (Ball ball : balls) {
			if (ball.isMoving()) {
				isRunning = true;
				/* The time that the first collision of this ball happens */
				double leastHitTime = 1000000000.0;
				/* The ball that this ball first hits with */
				Ball firstHitBall = null;
				for (Ball otherBall : balls)
					/* A ball cannot hit itself */
					if (!ball.equals(otherBall)) {
						double hitTime = Ball.getHitTime(ball, otherBall);
						if (hitTime != -1 && hitTime < 1.0 / 60
								&& hitTime < leastHitTime) {
							leastHitTime = hitTime;
							firstHitBall = otherBall;
						}
					}
				/*
				 * If the first collision happens within one update interval
				 * (1/60 s), the ball move until it hits another ball, hits, and
				 * moves in the new direction for the remaining time in that
				 * interval, so that hitting will not happen when two balls
				 * overlap.
				 */
				if (leastHitTime < 1.0 / 60) {
					ball.move(leastHitTime);
					Ball.ballHit(ball, firstHitBall);
					ball.move(1.0 / 60 - leastHitTime);
				} else
					ball.move();
			}
		}
		return isRunning;
	}
}
