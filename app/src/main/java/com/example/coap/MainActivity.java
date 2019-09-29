package com.example.coap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements NetworkDiscovery.INeighborChangedListener {

    //UdbDiscovery udbDiscovery;
    NSDiscovery nsDiscovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView viewById = findViewById(R.id.textView);
        viewById.setText(Utils.getIpAddress());
        /*findViewById(R.id.btn_udp_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                try {
                    udbDiscovery = new UdbDiscovery(Env.UDP_HOST, Env.UDP_PORT);
                    udbDiscovery.setNeighborRecordChanged(MainActivity.this);
                    udbDiscovery.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btn_udp_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.btn_udp_start).setEnabled(true);
                if (udbDiscovery != null) {
                    udbDiscovery.stop();
                }
            }
        });*/

        findViewById(R.id.btn_udp_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                nsDiscovery = new NSDiscovery();
                nsDiscovery.setNeighborChangedListener(MainActivity.this);
                nsDiscovery.registerService(MainActivity.this, "NSD-", Env.UDP_PORT);
            }
        });

        findViewById(R.id.btn_udp_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.btn_udp_start).setEnabled(true);
                if (nsDiscovery != null) {
                    nsDiscovery.stopNSDServer();
                }
            }
        });
    }

    @Override
    public void onNeighborChanged(final Collection<NeighborRecord> values) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tvLog = findViewById(R.id.tv_log);
                tvLog.setText(TextUtils.join("\n", values));
            }
        });
    }
}
