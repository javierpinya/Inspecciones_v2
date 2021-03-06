package clh.inspecciones.com.inspecciones_v2.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clh.inspecciones.com.inspecciones_v2.Adapters.CompartimentosAdapter;
import clh.inspecciones.com.inspecciones_v2.Clases.CACompartimentosBD;
import clh.inspecciones.com.inspecciones_v2.R;
import clh.inspecciones.com.inspecciones_v2.SingleTones.VolleySingleton;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddCompartimentosFragment extends Fragment implements RealmChangeListener<RealmResults<CACompartimentosBD>>, View.OnClickListener {

    private Realm realm;
    private RealmResults<CACompartimentosBD> caCompartimentosBD;
    private Button button;
    private dataListener callback;
    private static String matricula;
    private String inspeccion;
    private TextView cisterna;
    private CompartimentosAdapter adapter;
    private CACompartimentosBD compartimentosBD;
    private RealmList<CACompartimentosBD> compartimentosList;
    private String url = "http://pruebaalumnosandroid.esy.es/inspecciones/registrar_compartimentos.php";
    private String user;
    private String pass;

    //compartimentos
    private List<Integer> compartimentos;
    private List<Integer> capacidad;
    private List<String> tags;
    private List<Integer> cantidad;
    private List<Boolean> cumpleCantidad = new ArrayList<>();

    //FAB
    private FloatingActionButton fabCalculadora;
    private FloatingActionsMenu fabMenu;

    private RecyclerView mRecyclerView;
    private String soloVer="0";
    // Puede ser declarado como 'RecyclerView.Adapter' o como nuetra clase adaptador 'MyAdapter'


    public AddCompartimentosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            callback =(dataListener)context;
        }catch(Exception e){
            throw new ClassCastException(context.toString() + " should implement dataListener");

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;
        view = inflater.inflate(R.layout.fragment_add_compartimentos, container, false);
        realm = Realm.getDefaultInstance();
        cisterna = view.findViewById(R.id.tv_cisternamatricula);
        button = view.findViewById(R.id.btn_guardar);

        user = getArguments().getString("user", "no_user");
        pass = getArguments().getString("pass", "no_pass");
        matricula = getArguments().getString("cisterna", "sin_cisterna");
        inspeccion = getArguments().getString("inspeccion", "sin_inspeccion");
        soloVer = getArguments().getString("soloVer", "0");

        fabCalculadora = view.findViewById(R.id.fabCalculadora);
        fabMenu = view.findViewById(R.id.grupoFab);
        fabCalculadora.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                fabMenu.collapse();
                callback.abrirCalculadora();
            }
        });


        mRecyclerView = view.findViewById(R.id.rv_compartimentos);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mRecyclerView.setHasFixedSize(true);

        renderCompartimentos();
        cisterna.setText(caCompartimentosBD.get(0).getMatricula());
        caCompartimentosBD.addChangeListener(this);

        button.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;
    }


    public void renderCompartimentos(){

        caCompartimentosBD = realm.where(CACompartimentosBD.class).findAll();
        compartimentos = new ArrayList<>();
        capacidad = new ArrayList<>();
        tags = new ArrayList<>();
        cantidad = new ArrayList<>();

        if (caCompartimentosBD.size() > 0) {
            //Toast.makeText(getActivity(), "caCompartimentosBD.get(0).getCan_capacidad(): " + caCompartimentosBD.get(0).getCan_capacidad(), Toast.LENGTH_SHORT).show();

            for (int i = 0; i < caCompartimentosBD.size(); i++) {
                compartimentos.add (caCompartimentosBD.get(i).getCod_compartimento());
                capacidad.add(caCompartimentosBD.get(i).getCan_capacidad());
                tags.add(caCompartimentosBD.get(i).getCod_tag_cprt());
            }
        }else{
            Toast.makeText(getActivity(), "la query no da resultados...", Toast.LENGTH_SHORT).show();
        }

        adapter = new CompartimentosAdapter(caCompartimentosBD, R.layout.compartimentos_listview_item, new CompartimentosAdapter.OnItemClickListener(){

            @Override
            public void onItemClick(CACompartimentosBD compartimentosList, int position) {
                    dialogoIntroducirCantidad("Introducir cantidad cargada comp " + compartimentosList.getCod_compartimento(), compartimentosList.getCan_capacidad(), position);


            }
        });

        mRecyclerView.setAdapter(adapter);
    }

    //** Dialogs **/

    private void dialogoIntroducirCantidad(String title, final int capacidad, final int position){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        if (title != null) builder.setTitle(title);

        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.dialogo_cantidad_cargada, null);
        builder.setView(viewInflated);

        final EditText input = viewInflated.findViewById(R.id.et_cant_cargada);


        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int cantidad=1000000;
                Boolean cumple = false;
                String entrada=null;
                entrada = input.getText().toString().trim();
                if (entrada == null){
                    cantidad=0;
                    cumple=false;
                    //Toast.makeText(getActivity(), "Debe introducir una cantidad", Toast.LENGTH_LONG).show();
                }else{
                    cantidad = Integer.valueOf(entrada);
                }


                if (cantidad<capacidad){
                    //Toast.makeText(getActivity(), "Cumple", Toast.LENGTH_SHORT).show();
                    cumple=true;
                }else{
                    Toast.makeText(getActivity(), "No Cumple. La cantidad cargada supera la capacidad registrada", Toast.LENGTH_LONG).show();
                    cumple=false;
                }
                compartimentosBD = realm.where(CACompartimentosBD.class).equalTo("cod_compartimento", position+1).findFirst();
                realm.beginTransaction();
                compartimentosBD.setCan_cargada(cantidad);
                compartimentosBD.setCumple(cumple);
                realm.copyToRealmOrUpdate(compartimentosBD);
                realm.commitTransaction();


            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void guardar(){
        caCompartimentosBD = realm.where(CACompartimentosBD.class).findAll();
        for (int i=0;i<caCompartimentosBD.size();i++) {
            //registrar en BD Online
            guardarOnLine(user, pass,String.valueOf(caCompartimentosBD.get(i).getCod_compartimento()), caCompartimentosBD.get(i).getCod_tag_cprt(), String.valueOf(caCompartimentosBD.get(i).getCan_capacidad()), String.valueOf(caCompartimentosBD.get(i).getCan_cargada()), String.valueOf(caCompartimentosBD.get(i).getCumple()), inspeccion);
        }
    }

    private void guardarOnLine(final String user, final String pass, final String compartimento, final String tag, final String capacidad, final String cantidad, final String cumple, final String inspeccion) {

        StringRequest sr = new StringRequest(StringRequest.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
               // callback.continuar();  //mejor continuar para incluir observaciones, etc, no compartimentosGuardados
                callback.compartimentosGuardados(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }){


            @Override
            protected Map<String, String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("user", user);
                params.put("pass", pass);
                params.put("compartimento", compartimento);
                params.put("tag", tag);
                params.put("capacidad", capacidad);
                params.put("cantidad", cantidad);
                params.put("cumple", cumple);
                params.put("inspeccion", inspeccion);
                return params;
            }
        };

        VolleySingleton.getInstanciaVolley(getActivity()).addToRequestqueue(sr);
    }

    @Override
    public void onChange(RealmResults<CACompartimentosBD> caCompartimentosBDS) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_guardar:
                guardar();
                break;
                default:
                    break;
        }
    }

    public interface dataListener{
        void compartimentosGuardados(Boolean guardadoOk);
        void abrirCalculadora();
    }

}
