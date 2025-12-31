package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.SwitchableLight;

@TeleOp(name="colorSensor")
public class ColorSensorTeleOp extends LinearOpMode {
    // using HSV colors, so H is in degrees (must be divided by 360), S and V are in % (must be decimals)
    private final double greenLowerH = 81.0/360.0;
    private final double greenLowerS = .27; // not sure if we need this
    // private final double greenLowerV = .32; shouldn't need to use this
    private final double greenUpperH = 168.0/360.0;

    private final double purpleLowerH = 298.0/360.0;
    private final double purpleLowerS = .28;
//    private final double purpleLowerV = .25; shouldn't need to use this
    private final double purpleUpperH = 327.0/360.0;
    private enum BALL_STATUS {GREEN, PURPLE, NEITHER, CONFLICT}
    @Override
    public void runOpMode() throws InterruptedException {
        float[] colors1 = new float[3]; // colors1[0] = H, index 1 is S, 2 is V
        float[] colors2 = new float[3];
        NormalizedColorSensor color1 = hardwareMap.get(RevColorSensorV3.class, "color1");
        NormalizedColorSensor color2 = hardwareMap.get(RevColorSensorV3.class, "color2");
        // init
//        color1.setGain();
//        color2.setGain();
//        ((SwitchableLight)color1).enableLight(true);
//        ((SwitchableLight)color2).enableLight(true);

        waitForStart();
        while(opModeIsActive()) {
            Color.colorToHSV(color1.getNormalizedColors().toColor(), colors1);
            Color.colorToHSV(color2.getNormalizedColors().toColor(), colors2);
            BALL_STATUS sensorOneDecision = checkBall(colors1);
            BALL_STATUS sensorTwoDecision = checkBall(colors2);

            telemetry.addData("color1 colors", colors1);
            telemetry.addData("colors1 color chosen", sensorOneDecision);
            telemetry.addData("","");
            telemetry.addData("color2 colors", colors2);
            telemetry.addData("color2 color chosen", sensorTwoDecision);
            telemetry.addData("final color chosen",
                    combinedColorDecision(sensorOneDecision, sensorTwoDecision));
            telemetry.update();
        }
    }

    private BALL_STATUS checkBall(float[] colorsHSV) {
        if(colorsHSV[1] < 0.25) { // is a valid brightness to read colors from
            telemetry.addData("Not enough light", "");
            telemetry.update();
            return BALL_STATUS.NEITHER;
        }
        boolean isGreen = false;
        boolean isPurple = false;
        if(colorsHSV[0] > greenLowerH && colorsHSV[0] < greenUpperH && colorsHSV[1] > greenLowerS) {
            isGreen = true;
        }
        if(colorsHSV[0] > purpleLowerH && colorsHSV[0] < purpleUpperH && colorsHSV[1] > purpleLowerS) {
            isPurple = true;
        }

        telemetry.addData("isGreen?", isGreen);
        telemetry.addData("isPurple?", isPurple);
        telemetry.update();

        if(isGreen && isPurple) {
            telemetry.addData("CONFLICT", "Read both purple and green balls");
            telemetry.update();
            return BALL_STATUS.CONFLICT;
        }
        if(isGreen) return BALL_STATUS.GREEN;
        return BALL_STATUS.PURPLE;
    }

    private BALL_STATUS combinedColorDecision(BALL_STATUS sensorOneColor, BALL_STATUS sensorTwoColor) {
        if(sensorOneColor.equals(sensorTwoColor)) return sensorOneColor;
        else {
            telemetry.addData("CONFLICT", "sensor one detected " + sensorOneColor + " but sensor two detected " + sensorTwoColor);
            telemetry.update();
            return BALL_STATUS.CONFLICT;
        }
    }

}
