package co.infinum.easycamera.internal;

/**
 * Contains Device Manufacturer and Model.
 *
 * Models often have different ending depending where you bought the device. That is why equals supports regex-like comparison.
 * Example - SM-G930F is Samsung S7, last letter can be different so we use SM-G930* for device model.
 * Equals will return true for SM-G930* and SM-G930F.
 */
public class DeviceInfo {

    private final String manufacturer;

    private final String model;

    public DeviceInfo(String manufacturer, String model) {
        this.manufacturer = manufacturer;
        this.model = model;
    }

    @Override
    public boolean equals(Object o) {
        return o != null
                && o instanceof DeviceInfo
                && ((DeviceInfo) o).manufacturer != null && ((DeviceInfo) o).manufacturer.equals(this.manufacturer)
                && ((DeviceInfo) o).model != null && areModelsEqual(((DeviceInfo) o).model, this.model);
    }

    private boolean areModelsEqual(String model1, String model2) {
        return model1.length() == model2.length()
                && (model1.startsWith(model2.replace("*", "")) || model2.startsWith(model1.replace("*", "")));
    }
}