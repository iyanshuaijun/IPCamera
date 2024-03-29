// Tencent is pleased to support the open source community by making TNN available.
//
// Copyright (C) 2020 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

#ifndef ANDROID_HELPER_JNI_H_
#define ANDROID_HELPER_JNI_H_

#include <string>
#include <jni.h>
#define TNN_HELPER(sig) Java_com_ubeesky_ai_AINative_Helper_##sig
#ifdef __cplusplus
extern "C" {
#endif
std::string fdLoadFile(std::string path);
char* jstring2string(JNIEnv* env, jstring jstr);
jstring string2jstring(JNIEnv* env, const char* pat);
void setBenchResult(std::string result);
JNIEXPORT JNICALL jstring TNN_HELPER(getBenchResult)(JNIEnv *env, jobject thiz);
#ifdef __cplusplus
}
#endif

#endif // ANDROID_HELPER_JNI_H_
