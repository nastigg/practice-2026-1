import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Main extends JPanel {

    int width = 600;
    int height = 600;

    Sphere[] spheres = {
            new Sphere(0, 0, 3, 1, Color.RED),
            new Sphere(2, 0, 4, 1, Color.GREEN),
            new Sphere(-2, 0, 4, 1, Color.BLUE)
    };

    Vector light = new Vector(5, 5, -10);

    public static void main(String[] args) {
        JFrame frame = new JFrame("Mini Ray Tracer");
        Main panel = new Main();

        frame.add(panel);
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        panel.render();
    }

    void render() {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                double nx = (x - width / 2.0) / width;
                double ny = (y - height / 2.0) / height;

                Ray ray = new Ray(new Vector(0, 0, 0),
                        normalize(new Vector(nx, ny, 1)));

                Sphere hitSphere = null;
                double minT = Double.MAX_VALUE;

                for (Sphere s : spheres) {
                    Double t = intersect(ray, s);
                    if (t != null && t < minT) {
                        minT = t;
                        hitSphere = s;
                    }
                }

                int color = Color.BLACK.getRGB();

                if (hitSphere != null) {

                    Vector hitPoint = ray.origin.add(ray.dir.scale(minT));
                    Vector normal = normalize(hitPoint.sub(hitSphere.center));

                    Vector lightDir = normalize(light.sub(hitPoint));

                    double brightness = Math.max(0, dot(normal, lightDir));

                    // 🔹 тень
                    boolean inShadow = false;
                    Ray shadowRay = new Ray(hitPoint, lightDir);

                    for (Sphere s : spheres) {
                        if (s == hitSphere) continue;
                        if (intersect(shadowRay, s) != null) {
                            inShadow = true;
                            break;
                        }
                    }

                    if (inShadow) brightness *= 0.3;

                    int r = (int)(hitSphere.color.getRed() * brightness);
                    int g = (int)(hitSphere.color.getGreen() * brightness);
                    int b = (int)(hitSphere.color.getBlue() * brightness);

                    color = new Color(r, g, b).getRGB();
                }

                img.setRGB(x, y, color);
            }
        }

        Graphics g = getGraphics();
        g.drawImage(img, 0, 0, null);
    }

    Double intersect(Ray ray, Sphere s) {
        Vector oc = ray.origin.sub(s.center);

        double a = dot(ray.dir, ray.dir);
        double b = 2 * dot(oc, ray.dir);
        double c = dot(oc, oc) - s.r * s.r;

        double d = b * b - 4 * a * c;

        if (d < 0) return null;

        double t = (-b - Math.sqrt(d)) / (2 * a);
        return t > 0 ? t : null;
    }

    double dot(Vector a, Vector b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    Vector normalize(Vector v) {
        double len = Math.sqrt(dot(v, v));
        return new Vector(v.x / len, v.y / len, v.z / len);
    }

    static class Sphere {
        Vector center;
        double r;
        Color color;

        Sphere(double x, double y, double z, double r, Color c) {
            this.center = new Vector(x, y, z);
            this.r = r;
            this.color = c;
        }
    }

    static class Ray {
        Vector origin, dir;

        Ray(Vector o, Vector d) {
            origin = o;
            dir = d;
        }
    }

    static class Vector {
        double x, y, z;

        Vector(double x, double y, double z) {
            this.x = x; this.y = y; this.z = z;
        }

        Vector add(Vector v) {
            return new Vector(x+v.x, y+v.y, z+v.z);
        }

        Vector sub(Vector v) {
            return new Vector(x-v.x, y-v.y, z-v.z);
        }

        Vector scale(double s) {
            return new Vector(x*s, y*s, z*s);
        }
    }
}
