package co.infinum.easycamera;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by filipvinkovic on 20/07/16.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({CameraApi.FLASH_MODE_OFF, CameraApi.FLASH_MODE_ON, CameraApi.FLASH_MODE_AUTOMATIC})
public @interface FlashDef {

}
