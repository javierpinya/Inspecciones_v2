package clh.inspecciones.com.inspecciones_v2.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import clh.inspecciones.com.inspecciones_v2.Clases.CARigidoBD;
import clh.inspecciones.com.inspecciones_v2.Fragments.ControlAccesoResultadoRigidoFragment;
import clh.inspecciones.com.inspecciones_v2.R;

public class ControlAccesoResultadoRigidoActivity extends AppCompatActivity implements ControlAccesoResultadoRigidoFragment.dataListener{

    private String rigido;
    private String tipo_inspeccion;
    private String t_rigido;
   // public List<CARigidoBD> rigido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_acceso_resultado_rigido);
        if(getIntent().getExtras()!= null){
            rigido = getIntent().getStringExtra("vehiculo").trim();
            tipo_inspeccion = getIntent().getStringExtra("tipo_inspeccion").trim();
            t_rigido = getIntent().getStringExtra("t_rigido").trim();
            getRigidoIntent(rigido);

        }
    }

    @Override
    public void getRigidoIntent(String rigido) {
        ControlAccesoResultadoRigidoFragment controlAccesoResultadoRigidoFragment = (ControlAccesoResultadoRigidoFragment) getSupportFragmentManager().findFragmentById(R.id.ControlAccesoResultadoRigidoFragment);
        controlAccesoResultadoRigidoFragment.renderRigido(rigido);
    }
}
