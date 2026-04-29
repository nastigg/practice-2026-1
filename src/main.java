import javax.swing.*;
import java.awt.*;

public class Main extends JPanel {

    double angle = 0;

    double[][] vertices = {
            {-1,-1,-1},{1,-1,-1},{1,1,-1},{-1,1,-1},
            {-1,-1,1},{1,-1,1},{1,1,1},{-1,1,1}
    };

    int[][] edges = {
            {0,1},{1,2},{2,3},{3,0},
            {4,5},{5,6},{6,7},{7,4},
            {0,4},{1,5},{2,6},{3,7}
    };

    public static void main(String[] args) {
        JFrame frame = new JFrame("3D Cube");
        Main panel = new Main();

        frame.add(panel);
        frame.setSize(600,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        new Timer(16, e -> {
            panel.angle += 0.02;
            panel.repaint();
        }).start();
    }

    double[] rotateY(double x, double y, double z) {
        return new double[]{
                x * Math.cos(angle) - z * Math.sin(angle),
                y,
                x * Math.sin(angle) + z * Math.cos(angle)
        };
    }

    int[] project(double x, double y, double z) {
        double distance = 3;
        double factor = distance / (distance + z);

        int px = (int)(x * factor * 200 + 300);
        int py = (int)(y * factor * 200 + 300);

        return new int[]{px, py};
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int[][] p = new int[8][2];

        for (int i = 0; i < vertices.length; i++) {
            double[] r = rotateY(vertices[i][0], vertices[i][1], vertices[i][2]);
            p[i] = project(r[0], r[1], r[2]);
        }

        for (int[] e : edges) {
            g.drawLine(p[e[0]][0], p[e[0]][1],
                       p[e[1]][0], p[e[1]][1]);
        }
    }
}
