package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.SwitchableLight;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import java.util.List;

@TeleOp(name="colorSensor")
public class ColorSensorTeleOp extends LinearOpMode {
    // using HSV colors, so H is in degrees (must be divided by 360), S and V are in % (must be decimals)
    private final double greenLowerH = 81;
    private final double greenLowerS = .27; // not sure if we need this
    // private final double greenLowerV = .32; shouldn't need to use this
    private final double greenUpperH = 168.0;

    private final double purpleLowerH = 175.0;
    private final double purpleLowerS = .28;
//    private final double purpleLowerV = .25; shouldn't need to use this
    private final double purpleUpperH = 250.0;
    private enum BALL_STATUS {GREEN, PURPLE, NEITHER, CONFLICT}
    BALL_STATUS positionOne = BALL_STATUS.PURPLE;
    BALL_STATUS positionTwo = BALL_STATUS.PURPLE;
    BALL_STATUS positionThree = BALL_STATUS.GREEN;
    private enum BALL_ORDER {GPP, PPG, PGP, NONE};
    TelemetryManager panels;

    @Override
    public void runOpMode() throws InterruptedException {
        NormalizedColorSensor color1 = hardwareMap.get(RevColorSensorV3.class, "color1");
        NormalizedColorSensor color2 = hardwareMap.get(RevColorSensorV3.class, "color2");
        BALL_ORDER ballOrder;
        panels = PanelsTelemetry.INSTANCE.getTelemetry();
        // debug
//        telemetry.setAutoClear(false);
        panels.setUpdateInterval(500);
        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        LLResult result = limelight.getLatestResult();
        if (result.isValid()) {
            // Access general information
            List<LLResultTypes.FiducialResult> fiducialResults = result.getFiducialResults();
            for (LLResultTypes.FiducialResult fr : fiducialResults) {
                panels.debug("Fiducial", "ID: %d, Family: %s, X: %.2f, Y: %.2f", fr.getFiducialId(), fr.getFamily(), fr.getTargetXDegrees(), fr.getTargetYDegrees());
                if(fr.getFiducialId() == 21) ballOrder = BALL_ORDER.GPP;
                else if(fr.getFiducialId() == 22) ballOrder = BALL_ORDER.PGP;
                else if(fr.getFiducialId() == 23) ballOrder = BALL_ORDER.PPG;
                else ballOrder = BALL_ORDER.NONE;
            }
        }
        // id 21 = gpp , 22 = pgp , 23 = ppg+

        waitForStart();
        BALL_STATUS ballColor = getColorInPositionOne(color1, color2);
        positionOne = ballColor;
        while(opModeIsActive()) {
            ballColor = getColorInPositionOne(color1, color2);
//            // don't want to replace a ball color with neither or conflict, scan until a color is detected
//            if(!(positionOne.equals(BALL_STATUS.PURPLE) || positionOne.equals(BALL_STATUS.GREEN)) &&
//                    (ballColor.equals(BALL_STATUS.NEITHER) || ballColor.equals(BALL_STATUS.CONFLICT)))
            positionOne = ballColor;

            // on intake
//            if(gamepad1.a) {
//                int indexerRotations = getNumberOfRotationsUntilEmpty();
//                if(indexerRotations == 0) hw.intake.setPower(0);
//                else {
//                    hw.intake.setPower(1);
//                   rotateIndexerToIntake(indexerRotations);
//                }
//            }

            if (gamepad1.dpadRightWasPressed()) {
                rotateIndexerToOuttake(1);
            }
            if (gamepad1.dpadLeftWasPressed()) {
                rotateIndexerToIntake(1);
            }
            panels.addData("position one", positionOne);
            panels.addData("position two", positionTwo);
            panels.addData("position three", positionThree);
            panels.update(telemetry);
        }
    }

    private int searchBallColor(BALL_STATUS color) {
        if(positionThree.equals(color)) return 3;
        if(positionTwo.equals(color)) return 2;
        if(positionOne.equals(color)) return 1;
        return 0;
    }

    private void shootFromPosition(int position) {
        // 3 : 1 down 1 up
        // 2 : 1 up 1 down 1 up
        // 1 : 2 up 1 down 1 up
        switch (position) {
            case 3:
                rotateIndexerToOuttake(1);
                rotateIndexerToIntake(1);
                break;
            case 2:
                rotateIndexerToIntake(1);
                rotateIndexerToOuttake(1);
                rotateIndexerToIntake(1);
                break;
            case 1:
                rotateIndexerToIntake(2);
                rotateIndexerToOuttake(1);
                rotateIndexerToIntake(1);
                break;
            default:
                throw new IllegalArgumentException("Target position is not 1, 2, or 3");
        }
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
        if (colorsHSV[2] < 0.004) return BALL_STATUS.NEITHER;  // not enough light causes
        // purple to be read as green
        boolean isGreen = false;
        boolean isPurple = false;
        if(colorsHSV[0] > greenLowerH && colorsHSV[0] < greenUpperH && colorsHSV[1] > greenLowerS) {
            isGreen = true;
        }
        if(colorsHSV[0] > purpleLowerH && colorsHSV[0] < purpleUpperH && colorsHSV[1] > purpleLowerS) {
            isPurple = true;
        }

        panels.addData("isGreen?", isGreen);
        panels.addData("isPurple?", isPurple);

        if(isGreen && isPurple) {
            panels.addData("CONFLICT", "Read both purple and green balls");

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
            panels.addData("CONFLICT", "sensor one detected " + sensorOneColor + " but sensor two detected " + sensorTwoColor);
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

        for (float value : colors1) {
            panels.addData("color1 colors", value);
        }
        panels.addData("colors1 color chosen", sensorOneDecision);
        panels.addData("","");
        for (float value : colors2) {
            panels.addData("color2 colors", value);
        }
        panels.addData("color2 color chosen", sensorTwoDecision);
        panels.addData("final color chosen", ball_color);

        return ball_color;
    }
}
