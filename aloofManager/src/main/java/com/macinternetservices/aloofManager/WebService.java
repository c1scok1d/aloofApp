/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.macinternetservices.aloofManager;

import com.macinternetservices.aloofManager.model.Command;
import com.macinternetservices.aloofManager.model.CommandType;
import com.macinternetservices.aloofManager.model.Device;
import com.macinternetservices.aloofManager.model.Geofence;
import com.macinternetservices.aloofManager.model.Position;
import com.macinternetservices.aloofManager.model.Tracking;
import com.macinternetservices.aloofManager.model.User;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WebService {

    @FormUrlEncoded
    @POST("/api/session")
    Call<User> addSession(@Field("email") String email, @Field("password") String password);

    @GET("/api/devices")
    Call<List<Device>> getDevices();

    @GET("/api/devices")
    Call<List<Tracking>> getDeviceTracking(@Query("deviceId") String deviceId);

    @GET("/api/geofences")
    Call<List<Geofence>> getGeofences();

    @GET("/api/positions")
    Call<List<Position>> getPositions(@Query("deviceId") String deviceId, @Query("from") String lastTransitionStartTime, @Query("to") String lastTransitionEndTime);


  @POST("/api/devices")
    Call<Device> addDevice(@Header("Content-Type") String content_Type, @Body RequestBody requestBody);

    @PUT("/api/devices")
    Call<Device> editDevice(@Header("Content-Type") String content_Type, @Body RequestBody requestBody);

    @DELETE("/api/devices/{deviceId}")
    Call<Device> delDevice(@Header("Content-Type") String content_Type, @Path("deviceId") String deviceId);

    @POST("api/geofences")
    Call<Geofence> addGeoFence(@Header("Content-Type") String content_Type, @Body RequestBody requestBody);

    @GET("/api/commandtypes")
    Call<List<CommandType>> getCommandTypes(@Query("deviceId") long deviceId);

    @POST("/api/commands")
    Call<Command> sendCommand(@Body Command command);
}
