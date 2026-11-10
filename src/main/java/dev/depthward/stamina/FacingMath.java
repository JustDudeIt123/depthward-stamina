package dev.depthward.stamina;
/** Pure packet-angle calculations. A backward animation already reverses its movement. */
public final class FacingMath {
    public record Dodge(int animation, float yaw) {}
    public static float wrap(float degrees) { float n=degrees%360; if(n>=180)n-=360; if(n< -180)n+=360; return n; }
    public static float yawTo(double dx,double dz) { return wrap((float)Math.toDegrees(Math.atan2(dz,dx))-90); }
    public static Dodge roll(float cameraYaw,int forward,int left) {
        int v=Integer.signum(forward), h=Integer.signum(left);
        float offset=-(90*h*(1-Math.abs(v))+45*v*h);
        return new Dodge(v<0?1:0,wrap(cameraYaw+offset));
    }
    public static Dodge step(float cameraYaw,int forward,int left) {
        int v=Integer.signum(forward), h=Integer.signum(left);
        int animation=v==0?(h==0?0:(h>=0?2:3)):(v>=0?0:1);
        float offset=v==0?0:-(90*h*(1-Math.abs(v))+45*v*h);
        return new Dodge(animation,wrap(cameraYaw+offset));
    }
    private FacingMath() {}
}
