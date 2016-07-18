package co.infinum.easycamera;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by filipvinkovic on 18/07/16.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({CameraApi.CAMERA_FACING_BACK, CameraApi.CAMERA_FACING_FRONT})
public @interface CameraFacingDef {

}
