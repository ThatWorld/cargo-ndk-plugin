use jni::{
    JNIEnv,
    objects::{JObject, JString},
};

#[unsafe(no_mangle)]
pub extern "C" fn Java_io_github_cargo_ndk_plugin_MainActivity_helloFromNative<'a>(
    _env: JNIEnv<'a>,
    _thiz: JObject<'a>,
) -> JString<'a> {
    let message = "Rust!";
    let jstring = _env
        .new_string(message)
        .expect("Couldn't create Java string");
    jstring
}
