package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="protoAuto")
public class PrototypeAuto extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotor fl = hardwareMap.get(DcMotor.class, "fl");
        fl.setTargetPosition(0);
        fl.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        DcMotor fr = hardwareMap.get(DcMotor.class, "fr");
        fr.setTargetPosition(0);
        fr.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        DcMotor br = hardwareMap.get(DcMotor.class, "br");
        br.setTargetPosition(0);
        br.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        DcMotor bl = hardwareMap.get(DcMotor.class, "bl");
        bl.setTargetPosition(0);
        bl.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        waitForStart();
        ElapsedTime time = new ElapsedTime();
        if (isStopRequested()) return;
        fl.setPower(1);
        fr.setPower(1);
        bl.setPower(1);
        br.setPower(1);
        while (time.time() > 5) {
            fl.setPower(0);
            fr.setPower(0);
            bl.setPower(0);
            br.setPower(0);
        }

    }
}
