/**
 * Луч: начинается в точке origin и идёт в направлении direction.
 * Любая точка на луче: P(t) = origin + t * direction, где t >= 0.
 */
public class Ray {
    public final Vec3 origin;
    public final Vec3 direction; // всегда нормализован

    public Ray(Vec3 origin, Vec3 direction) {
        this.origin = origin;
        this.direction = direction.normalize();
    }

    /** Вычислить точку на луче при параметре t */
    public Vec3 at(double t) {
        return origin.add(direction.scale(t));
    }
}
