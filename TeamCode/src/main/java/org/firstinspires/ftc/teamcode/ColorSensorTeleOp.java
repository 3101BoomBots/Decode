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
    BALL_STATUS positionOne = BALL_STATUS.NEITHER;
    BALL_STATUS positionTwo = BALL_STATUS.NEITHER;
    BALL_STATUS positionThree = BALL_STATUS.NEITHER;

    @Override
    public void runOpMode() throws InterruptedException {
        NormalizedColorSensor color1 = hardwareMap.get(RevColorSensorV3.class, "color1");
        NormalizedColorSensor color2 = hardwareMap.get(RevColorSensorV3.class, "color2");

        // debug
        telemetry.setAutoClear(false);
        telemetry.setMsTransmissionInterval(500);

        // id 21 = gpp , 22 = pgp , 23 = ppg+
        // init
//        color1.setGain();  // multiplying, so only makes sense for >1
//        color2.setGain();
//        ((SwitchableLight)color1).enableLight(true);
//        ((SwitchableLight)color2).enableLight(true);

        waitForStart();
        BALL_STATUS ballColor = getColorInPositionOne(color1, color2);
        positionOne = ballColor;
        while(opModeIsActive()) {
            ballColor = getColorInPositionOne(color1, color2);
            // don't want to replace a ball color with neither or conflict, scan until a color is detected
            if(!(positionOne.equals(BALL_STATUS.PURPLE) || positionOne.equals(BALL_STATUS.GREEN)) &&
                    (ballColor.equals(BALL_STATUS.NEITHER) || ballColor.equals(BALL_STATUS.CONFLICT)))
                positionOne = ballColor;

            // on intake
            if(gamepad1.a) {
                int indexerRotations = getNumberOfRotationsUntilEmpty();
//                if(indexerRotations == 0) hw.intake.setPower(0);
//                else {
//                    hw.intake.setPower(1);
                   rotateIndexerToIntake(indexerRotations);
//                }
            }

        }
    }

    private int getNumberOfRotationsUntilEmpty() {
        if (isOccupied(positionOne) && isOccupied(positionTwo) && isOccupied(positionThree)) return 0;
        else if(positionOne.equals(BALL_STATUS.NEITHER)) return 0;
        else if(positionThree.equals(BALL_STATUS.NEITHER)) return 1;
        else if(positionTwo.equals(BALL_STATUS.NEITHER)) return 2;
        else return 0;
    }

    private boolean isOccupied(BALL_STATUS ball) {
        return ball.equals(BALL_STATUS.GREEN) || ball.equals(BALL_STATUS.PURPLE) || ball.equals(BALL_STATUS.CONFLICT);
    }
    public void rotateIndexerToIntake(int rotations) {
//        hw.indexerMotor.setTargetPosition(indexerMotor.getTargetPosition() - (int)(turns*0.333*INDEXER_RESOLUTION));
        BALL_STATUS temp;
        for(int i = 0; i < rotations; i++) {
            temp = positionThree;
            positionThree = positionTwo;
            positionTwo = positionOne;
            positionOne = temp;
        }
    }

    public void rotateIndexerToOuttake(int rotations) {
//        hw.indexerMotor.setTargetPosition(indexerMotor.getTargetPosition() - (int)(turns*0.333*INDEXER_RESOLUTION));
        BALL_STATUS temp;
        for(int i = 0; i < rotations; i++) {
            temp = positionTwo;
            positionTwo = positionThree;
            positionThree = positionOne;
            positionOne = temp;
        }
        positionTwo = BALL_STATUS.NEITHER;  // position two neither because it gets launched
    }

    private BALL_STATUS checkBall(float[] colorsHSV) {
        if(colorsHSV[2] < 0.20) { // is a valid brightness to read colors from
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
        if(isPurple) return BALL_STATUS.PURPLE;
        return BALL_STATUS.NEITHER;
    }

    private int rotationsToNearestColor(BALL_STATUS color) {
        if(positionThree.equals(color)) return 1;
        if(positionOne.equals(color)) return 2;
        if(positionTwo.equals(color)) return 3;
        return 0;
    }

    private BALL_STATUS combinedColorDecision(BALL_STATUS sensorOneColor, BALL_STATUS sensorTwoColor) {
        if(sensorOneColor.equals(sensorTwoColor)) return sensorOneColor;
        else if(sensorOneColor.equals(BALL_STATUS.NEITHER) || (sensorOneColor.equals(BALL_STATUS.CONFLICT) && !sensorTwoColor.equals(BALL_STATUS.NEITHER))) return sensorTwoColor;
        else if(sensorTwoColor.equals(BALL_STATUS.NEITHER) || sensorTwoColor.equals(BALL_STATUS.CONFLICT)) return sensorOneColor;
        else {
            telemetry.addData("CONFLICT", "sensor one detected " + sensorOneColor + " but sensor two detected " + sensorTwoColor);
            telemetry.update();
            return BALL_STATUS.CONFLICT;
        }
    }

    private BALL_STATUS getColorInPositionOne(NormalizedColorSensor color1, NormalizedColorSensor color2) {
        float[] colors1 = new float[3]; // colors1[0] = H, index 1 is S, 2 is V
        float[] colors2 = new float[3];

        Color.colorToHSV(color1.getNormalizedColors().toColor(), colors1);
        Color.colorToHSV(color2.getNormalizedColors().toColor(), colors2);
        BALL_STATUS sensorOneDecision = checkBall(colors1);
        BALL_STATUS sensorTwoDecision = checkBall(colors2);
        BALL_STATUS ball_color = combinedColorDecision(sensorOneDecision, sensorTwoDecision);

        telemetry.addData("color1 colors", colors1);
        telemetry.addData("colors1 color chosen", sensorOneDecision);
        telemetry.addData("","");
        telemetry.addData("color2 colors", colors2);
        telemetry.addData("color2 color chosen", sensorTwoDecision);
        telemetry.addData("final color chosen", ball_color);
        telemetry.update();

        return ball_color;
    }

}
