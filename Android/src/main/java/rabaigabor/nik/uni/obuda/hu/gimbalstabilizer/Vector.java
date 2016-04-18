package rabaigabor.nik.uni.obuda.hu.gimbalstabilizer;

/**
 * Created by TramNote on 2016.01.21..
 */
public class Vector {
    private float x;
    private float y;
    private float z;

    public Vector(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void Normalize()
    {
        double length = Math.sqrt(x * x + y * y + z * z);
        x /= length;
        y /= length;
        z /= length;
    }

    public Vector CrossProduct(Vector b)
    {
        return new Vector(y * b.z - z * b.y, -x * b.z + z * b.x, x * b.y - y * b.x);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }
}
