import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Main extends JPanel implements Runnable {

    private BufferedImage image;
    private double angle = 0;

    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    // Куб (8 вершин)
    double[][] vertices = {
            {-1, -1, -1}, {1, -1, -1},
            {1, 1, -1}, {-1, 1, -1},
            {-1, -1, 1}, {1, -1, 1},
            {1, 1, 1}, {-1, 1, 1}
    };

    int[][] edges = {
            {0,1},{1,2},{2,3},{3,0},
            {4,5},{5,6},{6,7},{7,4},
            {0,4},{1,5},{2,6},{3,7}
    };

    public Main() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        new Thread(this).start();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("3D Cube - Java");
        Main panel = new Main();
        frame.add(panel);
        frame.setSize(panel.WIDTH, panel.HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    @Override
    public void run() {
        while (true) {
            angle += 0.02;
            render();
            repaint();

            try { Thread.sleep(16); } catch (Exception ignored) {}
        }
    }

    private void render() {
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,WIDTH,HEIGHT);

        g.setColor(Color.WHITE);

        Point[] projected = new Point[vertices.length];

        for (int i = 0; i < vertices.length; i++) {
            double x = vertices[i][0];
            double y = vertices[i][1];
            double z = vertices[i][2];

            // rotation Y
            double xRot = x * Math.cos(angle) - z * Math.sin(angle);
            double zRot = x * Math.sin(angle) + z * Math.cos(angle);

            // perspective
            double distance = 3;
            double factor = distance / (distance - zRot);

            int xProj = (int)(xRot * factor * 100 + WIDTH / 2);
            int yProj = (int)(y * factor * 100 + HEIGHT / 2);

            projected[i] = new Point(xProj, yProj);
        }

        // draw edges
        for (int[] edge : edges) {
            Point p1 = projected[edge[0]];
            Point p2 = projected[edge[1]];
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        g.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }
}
