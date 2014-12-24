import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

/**
 * This class provides the frame for the menu and panel to display the game.
 * 
 * @author Guohong Yang
 *
 */
public class BilliardBallGame {

	private final JFrame window = new JFrame();
	private final GameMenu gameMenu = new GameMenu();
	private final GameCore gameCore = new GameCore();
	public static final int F = 2;
	public static final int TABLE_WIDTH = 357;
	public static final int TABLE_HEIGHT = 179;

	public BilliardBallGame() {
		gameCore.setPreferredSize(new Dimension(F * TABLE_WIDTH, F
				* TABLE_HEIGHT));
		window.setTitle("Billiard Ball");
		window.setJMenuBar(gameMenu);
		window.add(gameCore);
		window.setResizable(false);
		window.pack();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setFocusable(true);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}

	class GameMenu extends JMenuBar {
	}

	/**
	 * The engine of the game. It listens the mouse (or key in the future)
	 * events and notifies the white ball and the cue to respond accordingly
	 * 
	 * @author Guohong Yang
	 *
	 */
	public class GameCore extends JPanel implements MouseListener,
			MouseMotionListener {

		private Image background = new ImageIcon(getClass().getResource(
				"images/background.png")).getImage();
		private Point mouseLoc = new Point(0, 0);
		/* An object that contains all the balls and handles their movement */
		private BallFactory bf = new BallFactory();
		private int strength;
		private boolean isMouseDown;
		/* The initial configuration of the balls */
		private Ball whiteBall = new Ball(new double[] { 74,
				TABLE_HEIGHT * 0.5 + 15 }, new Color(240, 240, 240));
		private Ball blackBall = new Ball(new double[] { TABLE_WIDTH - 32.4,
				TABLE_HEIGHT * 0.5 }, Color.black);
		private Ball blueBall = new Ball(new double[] { TABLE_WIDTH * 0.5,
				TABLE_HEIGHT * 0.5 }, new Color(35, 35, 142));
		private Ball yellowBall = new Ball(new double[] { 74,
				TABLE_HEIGHT * 0.5 + 29 }, Color.yellow);
		private Ball greenBall = new Ball(new double[] { 74,
				TABLE_HEIGHT * 0.5 - 29 }, new Color(6, 70, 0));
		private Ball pinkBall = new Ball(new double[] { TABLE_WIDTH * 0.75,
				TABLE_HEIGHT * 0.5 }, new Color(218, 112, 214));
		private Ball brownBall = new Ball(
				new double[] { 74, TABLE_HEIGHT * 0.5 }, new Color(94, 38, 18));
		private Cue cue = new Cue(whiteBall);
		private double[] hitAngle;
		private boolean areBallsMoving = false;
		private boolean isCueHitting = false;

		public GameCore() {
			System.out.println(background.getWidth(this));
			addMouseListener(this);
			addMouseMotionListener(this);
			bf.add(whiteBall);
			bf.add(blackBall);
			bf.add(yellowBall);
			bf.add(greenBall);
			bf.add(blueBall);
			bf.add(pinkBall);
			bf.add(brownBall);
			/*
			 * Arrange the red balls in a triangle, with small distance between
			 * each two balls to avoid "overlapping" balls when they are moving
			 */
			for (int i = 0; i < 5; i++)
				for (int j = 0; j <= i; j++) {
					bf.add(new Ball(new double[] {
							TABLE_WIDTH * 0.75 + 2 * Ball.RADIUS + 1 + i
									* Ball.RADIUS * 1.74,
							TABLE_HEIGHT * 0.5 + i * (Ball.RADIUS + 0.5) - j
									* 2 * (Ball.RADIUS + 0.5) }, Color.red));
				}
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.drawImage(background, 0, 0, null);
			g2d.setColor(Color.white);
			g2d.drawLine(74 * F, 0, 74 * F, (int) (F * TABLE_HEIGHT));
			g2d.drawArc((74 - 29) * F, (TABLE_HEIGHT / 2 - 29) * F, 58 * F,
					58 * F, 90, 180);
			for (Ball ball : bf.getBalls()) {
				ball.drawBall(g2d);
			}
			if (!areBallsMoving)
				cue.drawCue(g2d, getMouseAngle(), strength);
			else if (isCueHitting) {
				cue.drawCue(g2d, hitAngle, strength);
			}
		}

		public void mouseClicked(MouseEvent me) {
		}

		public void mouseEntered(MouseEvent me) {
		}

		public void mouseExited(MouseEvent me) {
		}

		public void mousePressed(MouseEvent me) {
			if (!areBallsMoving) {
				isMouseDown = true;
				changeStrength();
			}
		}

		public void mouseReleased(MouseEvent me) {
			/* Only move the balls when they are not already moving */
			if (!areBallsMoving) {
				areBallsMoving = true;
				isMouseDown = false;
				hitAngle = getMouseAngle();
				Ball.cueHit(whiteBall, new double[] { -hitAngle[0] * strength,
						-hitAngle[1] * strength });
				Thread moveBalls = new Thread() {
					public void run() {
						isCueHitting = true;
						int i = 1;
						/* Display the animation of the moving cue */
						while (strength > -33) {
							strength -= i;
							i += 2;
							if (strength < -33)
								strength = -33;
							repaint();
							try {
								Thread.sleep(16);
							} catch (InterruptedException e) {
							}
						}
						isCueHitting = false;
						strength = 0;
						boolean isRunning = true;
						while (isRunning) {
							double now = System.nanoTime();
							/*
							 * ball factory return true if the any ball still
							 * have none zero speed, so that balls still need to
							 * be moved
							 */
							isRunning = bf.moveBalls();
							repaint();
							while (System.nanoTime() - now < 1000000000.0 / 60) {
								Thread.yield();
								try {
									Thread.sleep(1);
								} catch (Exception e) {
								}
							}
						}
						areBallsMoving = false;
						repaint();
					}
				};
				moveBalls.start();
			}
		}

		public void mouseDragged(MouseEvent me) {
			if (!areBallsMoving) {
				mouseLoc = me.getPoint();
				repaint();
			}
		}

		public void mouseMoved(MouseEvent me) {
			if (!areBallsMoving) {
				mouseLoc = me.getPoint();
				repaint();
			}
		}

		private double[] getMouseAngle() {
			double[] mouseAngle = new double[2];
			double distance = mouseLoc.distance(new Point((int) (whiteBall
					.getLoc()[0] * F), (int) (whiteBall.getLoc()[1] * F)));
			if (distance == 0)
				return null;
			mouseAngle[0] = (whiteBall.getLoc()[0] * F - mouseLoc.x) / distance;
			mouseAngle[1] = (whiteBall.getLoc()[1] * F - mouseLoc.y) / distance;
			return mouseAngle;
		}

		/**
		 * Increase or decrease the strength of your next hit when you hold you
		 * mouse
		 */
		private void changeStrength() {
			Thread changeStrength = new Thread() {
				public void run() {
					int i = 1;
					while (isMouseDown) {
						strength += 8 * i;
						repaint();
						if (strength <= 0 || strength >= 500)
							i *= -1;
						try {
							Thread.sleep(30);
						} catch (InterruptedException e) {
						}
					}
				}
			};
			changeStrength.start();
		}

	}
}
