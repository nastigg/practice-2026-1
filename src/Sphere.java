public class Sphere {
    public final Vec3 center;
    public final double radius;
    public final Material material;

    public Sphere(Vec3 center, double radius, Material material) {
        this.center = center;
        this.radius = radius;
        this.material = material;
    }

    public double intersect(Ray ray) {
        Vec3 oc = ray.origin.sub(center);      // вектор от центра к началу луча

        double a = ray.direction.dot(ray.direction); // = 1, т.к. direction нормализован
        double b = 2.0 * ray.direction.dot(oc);
        double c = oc.dot(oc) - radius * radius;

        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return -1; // луч не попал в сферу
        }

        double t = (-b - Math.sqrt(discriminant)) / (2.0 * a);
        if (t > 0.001) return t;

        t = (-b + Math.sqrt(discriminant)) / (2.0 * a);
        if (t > 0.001) return t;

        return -1;
    }

    public Vec3 normalAt(Vec3 point) {
        return point.sub(center).normalize();
    }
}
