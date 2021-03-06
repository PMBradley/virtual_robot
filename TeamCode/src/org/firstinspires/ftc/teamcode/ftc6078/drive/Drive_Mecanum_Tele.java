package org.firstinspires.ftc.teamcode.ftc6078.drive;

import com.qualcomm.robotcore.hardware.DcMotor;


public class Drive_Mecanum_Tele {

    // Create and initialize speed modifier variables - using default values (then can be set via a constructor)
    private double turnDivisor      = DEFAULT_TURN_DIVISOR; // what percentage of maximum turning speed should be used as a base turning speed (50% = 0.5, etc) - a multiplier
    private double translateDivisor = DEFAULT_TRANSLATE_DIVISOR; // what percentage of maximum translational speed should be used as a base translational speed (50% = 0.5, etc) - a multiplier
    private double boostingDivisor  = DEFAULT_BOOSTING_DIVISOR; // what percentage of maximum translational speed should be used as a boost translational speed (50% = 0.5, etc) - a multiplier

    // Default speed modifier values
    private static final double DEFAULT_TURN_DIVISOR = 0.6; // default values to use in the event no custom values are passed
    private static final double DEFAULT_TRANSLATE_DIVISOR = 0.6;
    private static final double DEFAULT_BOOSTING_DIVISOR = 1.0;


    //Motor variables
    private DcMotor driveFL, driveFR, driveBL, driveBR; // motors that are being used for mecanum driving
    private double powerFL, powerFR, powerBL, powerBR; // powers that are being passed to those motors


    // Default constructor
    public Drive_Mecanum_Tele(DcMotor driveMotorFL, DcMotor driveMotorFR, DcMotor driveMotorBL, DcMotor driveMotorBR){ // passing of individual motors in a constructor as an alternative to needing a robot class passed with the proper motor names
        // setup motors from passed motors
        driveFL = driveMotorFL;
        driveFR = driveMotorFR;
        driveBL = driveMotorBL;
        driveBR = driveMotorBR;
    }

    // Secondary constructor that can be used to pass different speed divisor values
    public Drive_Mecanum_Tele(DcMotor driveMotorFL, DcMotor driveMotorFR, DcMotor driveMotorBL, DcMotor driveMotorBR, double turnSpeed, double translateSpeed, double boostingSpeed){ // passing of individual motors in a constructor as an alternative to needing a robot class passed with the proper motor names
        // setup motors from passed motors
        driveFL = driveMotorFL;
        driveFR = driveMotorFR;
        driveBL = driveMotorBL;
        driveBR = driveMotorBR;

        // set custom values to the speed divisors
        turnDivisor = turnSpeed;
        translateDivisor = translateSpeed;
        boostingDivisor = boostingSpeed;
    }


    // Drive functions

    public void drive_field_relative(double x, double y, double r, double rawHeading, boolean isBoosting) { // use with controller only - this drives relative to field
        // if using controller inputs, reverse y in the arguments because down on the stick is positive and up is negative, and we need that to be the opposite way
        // if boosting is true, the robot will use the boostingDivisor instead of translateDivisor for speed setting

        if(isBoosting){ // if boosting, use the boosting divisor
            x = x * boostingDivisor; // multiply the speeds by the boostingDivisor (what percentage of max speed you want to be at while boosting)
            y = y * boostingDivisor;
        }
        else{ // if moving regularly, use the regular translate divisor
            x = x * translateDivisor; // multiply the speeds by the translateDivisor (what percentage of max speed you want to be at while moving normally)
            y = y * translateDivisor;
        }


        // adjust the raw heading (between -180 and 180) to be reversed and between 0 and 359.9999... to make the math easier later
        double heading = rawHeading * -1;
        if (heading >= 0) { // if the degree value is positive, subtract 360 from it until it is between 0 and 359.999....
            while (heading >= 360) {
                heading -= 360;
            }
        }
        else { // else it must be negative, so then add 360 to it until it is greater than or equal to 0 (and therefore must be between 0 and 359.999...
            while (heading < 0) {
                heading += 360;
            }
        }


        // Set up heading factor for relative to field (convert the heading to radians, then get the sine and cosine of that radian heading
        double sin = Math.sin(Math.toRadians(heading));
        double cos = Math.cos(Math.toRadians(heading));

        // do math to adjust to make the input drive vector relative to field (rather than relative to robot)
        double field_x = (x * cos) - (y * sin);
        double field_y = (x * sin) + (y * cos);

        // do math to get powers relative to field in addition to the cartesian mecanum formula
        powerFL = (field_y + (r * turnDivisor) + field_x);
        powerFR = -(field_y - (r * turnDivisor) - field_x);
        powerBL = (field_y + (r * turnDivisor) - field_x);
        powerBR = -(field_y - (r * turnDivisor) + field_x);


       // Unit Vector Normalization - Normalizes the translational inputs (ensure that all absolute values are less than or equal to 1, while maintaining the ratio between them)
       double magnitude = Math.sqrt(Math.pow(powerFL, 2) + Math.pow(powerFR, 2) + Math.pow(powerBL, 2) + Math.pow(powerBR, 2)); // get the total magnitude of the powers (square each one, add them, then get that square root)

       if(magnitude > 1.0){  // if the magnitude is over the max speed, divide all numbers by the largest number (meaning the largest number will become 1 and the rest scaled appropriately)
           powerFL /= magnitude;
           powerFR /= magnitude;
           powerBL /= magnitude;
           powerBR /= magnitude;
       }


        // set the motor powers based off of the math done previously
        driveFL.setPower(powerFL);
        driveFR.setPower(powerFR);
        driveBL.setPower(powerBL);
        driveBR.setPower(powerBR);
    }

    public void drive_robot_relative(double x, double y, double r, boolean isBoosting) { // use with controller only - this drives relative to the robot
        // if using controller inputs, reverse y in the arguments because down on the stick is positive and up is negative, and we need that to be the opposite way
        // if boosting is true, the robot will use the boostingDivisor instead of translateDivisor for speed setting

        if(isBoosting){ // if boosting, use the boosting divisor
            x = x * boostingDivisor; // multiply the speeds by the boostingDivisor (what percentage of max speed you want to be at while boosting)
            y = y * boostingDivisor;
        }
        else{ // if moving regularly, use the regular translate divisor
            x = x * translateDivisor; // multiply the speeds by the translateDivisor (what percentage of max speed you want to be at while moving normally)
            y = y * translateDivisor;
        }


        // do math to get powers set according to the cartesian mecanum formula
        powerFL = (y + (r * turnDivisor) + x);
        powerFR = -(y - (r * turnDivisor) - x);
        powerBL = (y + (r * turnDivisor) - x);
        powerBR = -(y - (r * turnDivisor) + x);


        // Unit Vector Normalization - Normalizes the translational inputs (ensure that all absolute values are less than or equal to 1, while maintaining the ratio between them)
        double magnitude = Math.sqrt(Math.pow(powerFL, 2) + Math.pow(powerFR, 2) + Math.pow(powerBL, 2) + Math.pow(powerBR, 2)); // get the total magnitude of the powers (square each one, add them, then get that square root)

        if(magnitude > 1.0){  // if the magnitude is over the max speed, divide all numbers by the largest number (meaning the largest number will become 1 and the rest scaled appropriately)
            powerFL /= magnitude;
            powerFR /= magnitude;
            powerBL /= magnitude;
            powerBR /= magnitude;
        }


        // set the motor powers based off of the math done previously
        driveFL.setPower(powerFL);
        driveFR.setPower(powerFR);
        driveBL.setPower(powerBL);
        driveBR.setPower(powerBR);
    }

}

