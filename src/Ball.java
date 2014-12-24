import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;

/**
 * A Ball represents a standard billiard ball. It contains the physical
 * properties of a standard billiard. It is responsible for calculating its own
 * location when moving.
 * 
 * @author Guohong Yang
 *
 */
public class Ball {
	private double[] ballLoc;
	private Color color;
	private double[] vel = { 0, 0 };
	private double[] tngVel = { 0, 0 };
	/* A value to indicate the state of the ball */
	private boolean isMoving;
	public static final double RADIUS = 2.625;
	/* Factor that causes a ball to slow down when it is in pure rotation */
	public static final double ROTATIONAL_FRICTION = 0.02;
	/* Factor that causes a ball to translate from sliding to pure rotation */
	public static final double FRICTION = 0.45;
	public static final double MASS = 0.1545;
	/* Factor that causes a ball to slow down after hitting the cushion */
	public static final double DAMP_FACTOR = -0.8;

	public Ball(double[] initLoc, Color c) {
		ballLoc = initLoc;
		color = c;
		isMoving = false;
	}

	/**
	 * return the time that two ball will collide. return -1 if they are not
	 * going to collide before any ball change its direction.
	 * 
	 * @param ball1
	 *            a ball to be calculated
	 * @param ball2
	 *            a ball to be calculated
	 * @return the time that the two balls will collide
	 */
	public static double getHitTime(Ball ball1, Ball ball2) {
		double constA = Math.pow(ball1.getVel()[0] - ball2.getVel()[0], 2)
				+ Math.pow(ball1.getVel()[1] - ball2.getVel()[1], 2);
		if (constA < 0.001)
			return -1;
		double constB = 2 * ((ball1.getLoc()[0] - ball2.getLoc()[0])
				* (ball1.getVel()[0] - ball2.getVel()[0]) + (ball1.getLoc()[1] - ball2
				.getLoc()[1]) * (ball1.getVel()[1] - ball2.getVel()[1]));
		if (constB >= 0)
			return -1;
		double constC = Math.pow(getDistance(ball1, ball2), 2) - 4 * RADIUS
				* RADIUS;
		double det = constB * constB - 4 * constA * constC;
		if (det < 0)
			return -1;
		return (-constB - Math.sqrt(det)) / (2 * constA);
	}

	public static double getDistance(Ball ball1, Ball ball2) {
		return Math.sqrt(Math.pow(ball1.getLoc()[0] - ball2.getLoc()[0], 2)
				+ Math.pow(ball1.getLoc()[1] - ball2.getLoc()[1], 2));
	}

	public static void cueHit(Ball ball, double[] impulse) {
		/* For easy change of how hard could we hit the ball */
		double factor = 1;
		ball.getVel()[0] = ball.getVel()[0] + factor * impulse[0];
		ball.getVel()[1] = ball.getVel()[1] + factor * impulse[1];
		ball.setIsMoving(true);
	}

	/**
	 * Calculate their respective momentum after the collision.
	 * 
	 * @param ball1
	 *            a ball to be calculated
	 * @param ball2
	 *            a ball to be calculated
	 */
	public static void ballHit(Ball ball1, Ball ball2) {
		double[] angle = new double[2];
		double distance = getDistance(ball1, ball2);
		angle[0] = (ball2.getLoc()[0] - ball1.getLoc()[0]) / distance;
		angle[1] = (ball2.getLoc()[1] - ball1.getLoc()[1]) / distance;
		double c2 = Math.pow(angle[0], 2);
		double sc = angle[0] * angle[1];
		double s2 = Math.pow(angle[1], 2);
		double[] v1 = ball1.getVel();
		double[] v2 = ball2.getVel();
		double newV1X = v2[0] * c2 + v2[1] * sc - v1[1] * sc + v1[0] * s2;
		double newV1Y = v2[0] * sc + v2[1] * s2 + v1[1] * c2 - v1[0] * sc;
		double newV2X = v1[0] * c2 + v1[1] * sc - v2[1] * sc + v2[0] * s2;
		double newV2Y = v1[0] * sc + v1[1] * s2 + v2[1] * c2 - v2[0] * sc;
		ball1.getVel()[0] = newV1X;
		ball1.getVel()[1] = newV1Y;
		ball2.getVel()[0] = newV2X;
		ball2.getVel()[1] = newV2Y;
		ball1.setIsMoving(true);
		ball2.setIsMoving(true);
	}

	public void move() {
		move(1.0 / 60);
	}

	/**
	 * move the ball in its current direction. Check whether the ball hits the
	 * cushion. Velocity and spin of the ball is calculated and updated.
	 * 
	 * @param time
	 */
	public void move(double time) {
		if (getSpd() == 0 && getTngSpd() == 0) {
			isMoving = false;
			return;
		}
		ballLoc[0] += vel[0] * time;
		ballLoc[1] += vel[1] * time;
		/*
		 * If the ball hits the cushions, velocity is reversed and decreased in
		 * the impeding direction.
		 */
		if (ballLoc[0] + RADIUS > BilliardBallGame.TABLE_WIDTH) {
			ballLoc[0] = BilliardBallGame.TABLE_WIDTH - RADIUS;
			if (tngVel[0] * (tngVel[0] - vel[0]) < 0) {
				tngVel[0] = 0;
			} else
				tngVel[0] -= vel[0];
			vel[0] *= DAMP_FACTOR;
		}
		if (ballLoc[0] - RADIUS < 0) {
			ballLoc[0] = RADIUS;
			if (tngVel[0] * (tngVel[0] - vel[0]) < 0)
				tngVel[0] = 0;
			else
				tngVel[0] -= vel[0];
			vel[0] *= DAMP_FACTOR;
		}
		if (ballLoc[1] + RADIUS > BilliardBallGame.TABLE_HEIGHT) {
			ballLoc[1] = BilliardBallGame.TABLE_HEIGHT - RADIUS;
			if (tngVel[1] * (tngVel[1] - vel[1]) < 0)
				tngVel[1] = 0;
			else
				tngVel[1] -= vel[1];
			vel[1] *= DAMP_FACTOR;
		}
		if (ballLoc[1] - RADIUS < 0) {
			ballLoc[1] = RADIUS;
			if (tngVel[1] * (tngVel[1] - vel[1]) < 0)
				tngVel[1] = 0;
			else
				tngVel[1] -= vel[1];
			vel[1] *= DAMP_FACTOR;
		}
		/* The difference between the rotation and linear velocity */
		double[] delVel = new double[] { tngVel[0] - vel[0], tngVel[1] - vel[1] };
		double delSpd = Math
				.sqrt(delVel[0] * delVel[0] + delVel[1] * delVel[1]);
		double[] rotationalFriction = new double[2];
		/* The speed of the ball is decreased due to the rotational friction */
		if (getSpd() != 0) {
			rotationalFriction[0] = -ROTATIONAL_FRICTION * vel[0] / getSpd();
			rotationalFriction[1] = -ROTATIONAL_FRICTION * vel[1] / getSpd();
		}
		double[] friction = new double[] { 0, 0 };
		/*
		 * If the difference between the rotational and linear speed exists ,
		 * one will translate to the other. If the difference is small enough,
		 * they are set to equal to avoid infinite translation.
		 */
		if (delSpd * delSpd > 0.04) {
			friction[0] = FRICTION * delVel[0] / delSpd;
			friction[1] = FRICTION * delVel[1] / delSpd;
			double newVelX = vel[0] + (friction[0] + rotationalFriction[0])
					/ MASS * time * 100;
			double newVelY = vel[1] + (friction[1] + rotationalFriction[1])
					/ MASS * time * 100;
			double newTngVelX = tngVel[0] - 2.5 * friction[0] / MASS * time
					* 100;
			double newTngVelY = tngVel[1] - 2.5 * friction[1] / MASS * time
					* 100;
			if ((vel[0] - tngVel[0]) * (newVelX - newTngVelX) < 0) {
				vel[0] = (newVelX + newTngVelX) / 2;
				tngVel[0] = vel[0];
			} else {
				vel[0] = newVelX;
				tngVel[0] = newTngVelX;
			}
			if ((vel[1] - tngVel[1]) * (newVelY - newTngVelY) < 0) {
				vel[1] = (newVelY + newTngVelY) / 2;
				tngVel[1] = vel[1];
			} else {
				vel[1] = newVelY;
				tngVel[1] = newTngVelY;
			}
		} else {
			double newVelX = vel[0] + 5.0 / 7 * rotationalFriction[0] / MASS * time
					* 100;
			double newVelY = vel[1] + 5.0 / 7 * rotationalFriction[1] / MASS * time
					* 100;
			if (newVelX * vel[0] < 0) {
				vel = new double[] { 0, 0 };
				tngVel = new double[] { 0, 0 };
			} else {
				vel[0] = newVelX;
				vel[1] = newVelY;
				tngVel[0] = vel[0];
				tngVel[1] = vel[1];
			}
		}

	}

	public double[] getLoc() {
		return ballLoc;
	}

	public double[] getVel() {
		return vel;
	}

	public boolean isMoving() {
		return isMoving;
	}

	public void setIsMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}

	public void drawBall(Graphics2D g) {
		g.setColor(color);
		g.fillOval((int) ((ballLoc[0] - RADIUS) * BilliardBallGame.F),
				(int) ((ballLoc[1] - RADIUS) * BilliardBallGame.F),
				(int) (2 * RADIUS * BilliardBallGame.F),
				(int) (2 * RADIUS * BilliardBallGame.F));
	}

	public double getSpd() {
		return Math.sqrt(vel[0] * vel[0] + vel[1] * vel[1]);
	}

	public double getTngSpd() {
		return Math.sqrt(tngVel[0] * tngVel[0] + tngVel[1] * tngVel[1]);
	}

	/**
	 * for test purpose.
	 */
	private void printVelocities() {
		DecimalFormat df = new DecimalFormat("#.0000");
		System.out.println("vel x: " + df.format(vel[0]) + ", vel y: "
				+ df.format(vel[1]));
		System.out.println("tngVel x: " + df.format(tngVel[0]) + ", tngVel y: "
				+ df.format(tngVel[1]));
		System.out.println();
	}

	/**
	 * Try to draw shadow for a ball but the effect is not good.
	 * @param g2d
	 */
	private void drawShadow(Graphics2D g2d) {
		int h = 150;
		double delD2 = Math.pow(ballLoc[0] - 127, 2)
				+ Math.pow(ballLoc[1] - 63.5, 2);
		double rotateAngle = Math.acos(-(127 - ballLoc[0]) / Math.sqrt(delD2));
		if (ballLoc[1] < 63.5)
			rotateAngle *= -1;
		double i = Math.pow(h - RADIUS, 2) + delD2;
		double a = h
				* RADIUS
				* i
				* Math.sqrt(i - RADIUS * RADIUS)
				/ ((i - RADIUS * RADIUS) * Math.pow(h - RADIUS, 2) - RADIUS
						* RADIUS * delD2);
		double b = h * RADIUS / Math.sqrt(h * h - 2 * h * RADIUS);
		double c = Math.sqrt(a * a - b * b);
		Ellipse2D shadow = new Ellipse2D.Double(3 * (ballLoc[0] + c - a),
				3 * (ballLoc[1] - b), 2 * a * BilliardBallGame.F, 2 * b
						* BilliardBallGame.F);
		AffineTransform transform = AffineTransform.getRotateInstance(
				rotateAngle, ballLoc[0] * BilliardBallGame.F, ballLoc[1]
						* BilliardBallGame.F);
		Shape rotatedShadow = transform.createTransformedShape(shadow);
		g2d.setColor(new Color(0, 0, 0, 64));
		g2d.fill(rotatedShadow);
	}
}
