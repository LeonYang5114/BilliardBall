import java.awt.Color;
import java.awt.Graphics;

public class Cue {

	private Ball whiteBall;

	public Cue(Ball wb) {
		whiteBall = wb;
	}

	public void drawCue(Graphics g, double[] angle, int strength) {
		if (angle == null)
			return;
		int whiteX = (int) (whiteBall.getLoc()[0] * BilliardBallGame.F);
		int whiteY = (int) (whiteBall.getLoc()[1] * BilliardBallGame.F);
		int x1 = (int) (whiteX + angle[0] * (20 + strength / 3) + angle[1] * 1);
		int x2 = (int) (whiteX + angle[0] * (20 + strength / 3) - angle[1] * 1);
		int x3 = (int) (whiteX + angle[0] * (20 + 145 * BilliardBallGame.F + strength / 3) - angle[1] * 5);
		int x4 = (int) (whiteX + angle[0] * (20 + 145 * BilliardBallGame.F + strength / 3) + angle[1] * 5);
		int y1 = (int) (whiteY + angle[1] * (20 + strength / 3) - angle[0] * 1);
		int y2 = (int) (whiteY + angle[1] * (20 + strength / 3) + angle[0] * 1);
		int y3 = (int) (whiteY + angle[1] * (20 + 145 * BilliardBallGame.F + strength / 3) + angle[0] * 5);
		int y4 = (int) (whiteY + angle[1] * (20 + 145 * BilliardBallGame.F + strength / 3) - angle[0] * 5);

		g.setColor(new Color(120, 50, 0));
		g.fillPolygon(new int[] { x1, x2, x3, x4 },
				new int[] { y1, y2, y3, y4 }, 4);
	}
}
