package com.example.lawnmower;

public class ErrorMessageGenerator {

    public static AppControlsProtos.LawnmowerStatus BuildError (int i) {
        AppControlsProtos.LawnmowerStatus error ;

     switch(i) {
         case (0):
             error = AppControlsProtos.LawnmowerStatus.newBuilder().setError(AppControlsProtos.LawnmowerStatus.Error.NO_ERROR).build();
             break;
         case (1):
            error = AppControlsProtos.LawnmowerStatus.newBuilder().setError(AppControlsProtos.LawnmowerStatus.Error.ROBOT_STUCK).build();
            break;
        case(2):
            error = AppControlsProtos.LawnmowerStatus.newBuilder().setError(AppControlsProtos.LawnmowerStatus.Error.BLADE_STUCK).build();
            break;
        case(3):
            error = AppControlsProtos.LawnmowerStatus.newBuilder().setError(AppControlsProtos.LawnmowerStatus.Error.PICKUP).build();
            break;
        case(4):
            error = AppControlsProtos.LawnmowerStatus.newBuilder().setError(AppControlsProtos.LawnmowerStatus.Error.LOST).build();
            break;
        default:
            error = null;
            break;
    }
        return error;
}
}