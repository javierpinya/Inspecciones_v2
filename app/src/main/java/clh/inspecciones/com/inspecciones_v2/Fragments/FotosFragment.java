package clh.inspecciones.com.inspecciones_v2.Fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import clh.inspecciones.com.inspecciones_v2.Adapters.CameraAdapter;
import clh.inspecciones.com.inspecciones_v2.Adapters.FotosAdapter;
import clh.inspecciones.com.inspecciones_v2.Clases.FotosBD;
import clh.inspecciones.com.inspecciones_v2.R;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;

/**
 * A simple {@link Fragment} subclass.
 */
public class FotosFragment extends Fragment {

    private GridView gridView;
    private List<ImageView> imageViews=new ArrayList<>();
    private Bitmap imagen;
    private List<Uri> path;
    private List<Bitmap> bitmaps;

    private FotosAdapter myAdapter;
    private CameraAdapter myCameraAdapter;
    private List<ImageView> mImageViews = new ArrayList<ImageView>();
    private dataListener callback;
    private List<FotosBD> fotos = new ArrayList<>();
    private FloatingActionButton fab;
    //private ImageView imagen;
    private String RUTA_IMAGEN="";
    //private String path;
    final int COD_SELECCION=10;
    static final int COD_CAMARA=20;
    private String bitmapConvertida;
    private Bitmap bitmapConvertida2;


    public FotosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            callback = (FotosFragment.dataListener)context;
        }catch(Exception e){
            throw new ClassCastException(context.toString() + " should implement EnviarData");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fotos, container, false);
        gridView = (GridView)view.findViewById(R.id.gridView);
        path = new ArrayList<>();
        bitmaps = new ArrayList<>();


        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cargarImagen();
            }
        });

        if(validaPermisos()){
            fab.show();
        }else{
            fab.hide();
        }
        // Inflate the layout for this fragment
        return view;
    }

    private void cargarImagen() {
        final CharSequence[] opciones={"Camara", "Galeria", "Cancelar"};
        final AlertDialog.Builder alertOpciones = new AlertDialog.Builder(getActivity());
        alertOpciones.setTitle("Seleccione una opción");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (opciones[which].toString()){
                    case "Camara":
                        try {
                            tomarFoto();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        //Toast.makeText(getActivity(), "Tomando Foto", Toast.LENGTH_SHORT).show();
                        break;
                    case "Galeria":
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/");
                        startActivityForResult(intent.createChooser(intent, "Seleccione la aplicacion"), COD_SELECCION);
                        break;
                    case "Cancelar":
                        dialog.dismiss();
                }
            }
        });

        alertOpciones.show();
    }

    private void tomarFoto() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        String storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + imageFileName;
        File image = new File(storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        RUTA_IMAGEN = image.getAbsolutePath();


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            String authorities=getActivity().getApplicationContext().getPackageName()+ ".provider";
            Uri imageUri = FileProvider.getUriForFile(getActivity(), authorities, image);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }else{
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
        }
        startActivityForResult(intent, COD_CAMARA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap=null;

        if(resultCode==RESULT_OK){
            switch (requestCode){
                case COD_SELECCION:
                    Uri miPath = data.getData();

                    //imageViews.add(imagen);
                    path.add(miPath);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),miPath);
                        bitmapConvertida2 = convertirBitmap2(bitmap);
                        bitmaps.add(bitmapConvertida2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //myAdapter = new FotosAdapter(getActivity(), R.layout.list_fotos, path);
                    //gridView.setAdapter(myAdapter);
                    //myAdapter.notifyDataSetChanged();
                    break;
                case COD_CAMARA:
                    MediaScannerConnection.scanFile(getContext(), new String[]{RUTA_IMAGEN}, null, new MediaScannerConnection.OnScanCompletedListener(){

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("Ruta de almacenamiento", "Path: " + path);
                        }
                    });

                    bitmap = BitmapFactory.decodeFile(RUTA_IMAGEN);
                    bitmapConvertida = convertirBitmap(bitmap);
                    bitmapConvertida2 = convertirBitmap2(bitmap);
                    bitmaps.add(bitmapConvertida2);

                    break;
            }
            myCameraAdapter = new CameraAdapter(getActivity(), R.layout.list_fotos, bitmaps);
            gridView.setAdapter(myCameraAdapter);
            myCameraAdapter.notifyDataSetChanged();
            callback.guardarFotos(bitmap);
        }
    }

    private Bitmap convertirBitmap2(Bitmap bitmap) {
        ByteArrayOutputStream array = new ByteArrayOutputStream();
        int size = bitmap.getDensity();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, array);
        if (size == bitmap.getDensity()){
            Toast.makeText(getActivity(), "No se ha comprimido", Toast.LENGTH_SHORT).show();
        }
        return bitmap;
    }

    private String convertirBitmap(Bitmap bitmap) {
        ByteArrayOutputStream array = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, array);
        byte[] imagenByte = array.toByteArray();
        String imagenString = Base64.encodeToString(imagenByte, Base64.DEFAULT);

        return imagenString;
    }

    private boolean validaPermisos(){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return true;
        }
        if((checkSelfPermission(getActivity(),CAMERA) == PackageManager.PERMISSION_GRANTED && (checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))){
            return true;
        }
        if((shouldShowRequestPermissionRationale(CAMERA)) || (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE))){
            cargarDialogoRecomendacion();
        }else{
            requestPermissions(new String[] {WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100){
            if(grantResults.length==2 && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED){
                fab.show();
            }else{
                solicitarPermisosManual();
            }
        }
    }

    private void solicitarPermisosManual() {
        final CharSequence[] opciones={"si", "no"};
        final AlertDialog.Builder alertOpciones= new AlertDialog.Builder(getActivity());
        alertOpciones.setTitle("¿Desea configurar los permisos de forma manual?");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(opciones[which].equals("si")){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }else{
                    Toast.makeText(getActivity(), "Los permisos no fueron aceptados", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
    }

    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(getActivity());
        dialogo.setTitle("Permisos desactivados");
        dialogo.setMessage("Debe aceptar los permisos para tomar fotos");
        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[] {WRITE_EXTERNAL_STORAGE, CAMERA}, 100);;
            }
        });
        dialogo.show();
    }

    public void renderFotos(List<Uri> path){
        //this.mImageViews.add(imageView);

        //fotos.set(0, imageView);
        myAdapter = new FotosAdapter(getActivity(), R.layout.list_fotos, path);
        gridView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
    }

    public interface dataListener{
        void guardarFotos(Bitmap bitmap);
    }

}