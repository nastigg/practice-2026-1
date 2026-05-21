/**
 * Точечный источник света.
 * position — координаты в пространстве
 * intensity — яркость (RGB)
 */
public class Light {
    public final Vec3 position;
    public final Vec3 intensity;

    public Light(Vec3 position, Vec3 intensity) {
        this.position = position;
        this.intensity = intensity;
    }
}
