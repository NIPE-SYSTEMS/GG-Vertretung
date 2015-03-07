/*
 * Copyright (C) 2015 Hauke Oldsen
 *
 * This file is part of GGVertretungsplan.
 *
 * GGVertretungsplan is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GGVertretungsplan is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GGVertretungsplan.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.gebatzens.ggvertretungsplan;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import java.net.URISyntaxException;

public class MQTTService extends IntentService {

    int id = 1000;

    public MQTTService() {
        super("GGService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MQTT client = new MQTT();
        try {
            client.setHost("tcp://192.168.177.29:1883");
        } catch (URISyntaxException e) {
            Log.e("ggmqtt", "Failed to set Host", e);
        }
        client.setConnectAttemptsMax(1);
        BlockingConnection con = client.blockingConnection();
        try {
            con.connect();
            Log.w("ggmqtt", "Connected");
            con.subscribe(new Topic[]{new Topic("gg/test", QoS.AT_LEAST_ONCE)});
            while(true) {
                Message message = con.receive();
                String msg = new String(message.getPayload(), "UTF-8");
                Log.i("ggmqtt", "RECEIVED MESSAGE " + message.getTopic() + " " + msg);
                String[] s = msg.split(";");
                if(s.length > 1)
                    GGApp.GG_APP.createNotification(R.drawable.ic_gg_star, s[0], s[1], new Intent(this, MainActivity.class), id++, "test");
                message.ack();
            }
        } catch (Exception e) {
            Log.e("ggmqtt", "Failed to connect to server", e);
        }
    }
}
