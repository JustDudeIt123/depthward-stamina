package dev.depthward.stamina;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class FacingMathTest {
    @Test void cardinalRollsUseCameraAndRespectBackwardAnimation() {
        assertEquals(0,FacingMath.roll(0,1,0).yaw());
        assertEquals(-90,FacingMath.roll(0,0,1).yaw());
        assertEquals(90,FacingMath.roll(0,0,-1).yaw());
        var back=FacingMath.roll(0,-1,0);
        assertEquals(1,back.animation()); assertEquals(-180,FacingMath.wrap(back.yaw()+180));
    }
    @Test void allEightRollDirectionsStayDistinct() {
        var yaws=new java.util.HashSet<Float>();
        for(int v=-1;v<=1;v++) for(int h=-1;h<=1;h++) {
            if(v==0 && h==0)continue;
            var p=FacingMath.roll(37,v,h);
            yaws.add(FacingMath.wrap(p.yaw()+(p.animation()==1?180:0)));
        }
        assertEquals(8,yaws.size());
    }
    @Test void stepLateralAnimationsKeepTheirOwnLateralMotion() {
        assertEquals(2,FacingMath.step(25,0,1).animation());
        assertEquals(25,FacingMath.step(25,0,1).yaw());
        assertEquals(3,FacingMath.step(25,0,-1).animation());
        assertEquals(1,FacingMath.step(25,-1,0).animation());
    }
    @Test void targetAndCameraAreIndependentAndAnglesWrap() {
        assertEquals(0,FacingMath.yawTo(0,4));
        assertEquals(-90,FacingMath.yawTo(4,0));
        assertEquals(-180,FacingMath.yawTo(0,-4));
        assertEquals(90,FacingMath.yawTo(-4,0));
        assertEquals(-171,FacingMath.roll(189,1,0).yaw());
        assertEquals(-81,FacingMath.roll(189,0,-1).yaw());
    }
}
