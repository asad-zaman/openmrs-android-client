package org.intelehealth.app.mpower.activities.dynamicForm

import com.google.gson.annotations.SerializedName

/*sealed class DynamicFormItem {

    data class TextItem(
        @SerializedName("id") val id: String,
        @SerializedName("hint") var hint: String,
        @SerializedName("type") var type: String,
        @SerializedName("value") var value: String
    ) : DynamicFormItem()

    data class EditTextItem(
        @SerializedName("id") val id: String,
        @SerializedName("hint") var hint: String,
        @SerializedName("type") var type: String,
        @SerializedName("value") var value: String
    ) : DynamicFormItem()

    data class DropdownItem(
        @SerializedName("id") val id: String,
        @SerializedName("hint") var hint: String,
        @SerializedName("type") var type: String,
        @SerializedName("value") var value: String,
        @SerializedName("options") var options: List<String>
    ) : DynamicFormItem()

    data class RadioButtonItem(
        @SerializedName("id") val id: String,
        @SerializedName("hint") var hint: String,
        @SerializedName("type") var type: String,
        @SerializedName("value") var value: String,
        @SerializedName("options") var options: List<String>
    ) : DynamicFormItem()
}*/

data class DynamicFormItem(
    @SerializedName("id") val id: String,
    @SerializedName("hint") var hint: String,
    @SerializedName("type") var type: String,
    @SerializedName("value") var value: String,
    @SerializedName("options") var options: List<String>?
)