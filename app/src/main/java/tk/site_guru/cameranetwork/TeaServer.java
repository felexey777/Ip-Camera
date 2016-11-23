package tk.site_guru.cameranetwork;

import java.io.*;
import java.util.*;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.widget.Toast;

public class TeaServer extends NanoHTTPD implements Camera.PreviewCallback
{
    static final String TAG="TEAONLY";
    ByteArrayOutputStream filecon;
    byte[] photo;
    boolean click = true;

    public TeaServer(int port, Context ctx) throws IOException {
        super(port, ctx.getAssets());
    }
    
    public TeaServer(int port, String wwwroot) throws IOException {
        super(port, new File(wwwroot).getAbsoluteFile());
    }

    @Override
    public Response serve( String uri, String method, Properties header, Properties parms, Properties files ) {
        Log.d(TAG, "httpd request >>" + method + " '" + uri + "' " + "   " + parms);

       if ( uri.startsWith("/img/") ) {
            return serveCGI(uri, method, header, parms, files);
        //} else if ( uri.startsWith("/stream/") ) {
         //   return serveStream(uri, method, header, parms, files);
        } else {
           return super.serve(uri, method, header, parms, files);
       }
       // return new Response();
	}

    public Response serveStream( String uri, String method, Properties header, Properties parms, Properties files ) {
        CommonGatewayInterface cgi = cgiEntries.get(uri);
        if ( cgi == null)
            return null;

        InputStream ins;
        ins = cgi.streaming(parms);
        if ( ins == null)
            return null;

        Random rnd = new Random();
        String etag = Integer.toHexString( rnd.nextInt() );
        String mime = parms.getProperty("mime");
        if ( mime == null)
            mime = "application/octet-stream";
        Response res = new Response( HTTP_OK, mime, ins);
        res.addHeader( "ETag", etag);
        res.isStreaming = true; 
        
        return res;
    }

    public Response serveCGI( String uri, String method, Properties header, Properties parms, Properties files ) {

        
      //  String msg = "hello! graber a spoon!" +" "  + uri + " " +  method +  " " + header + parms + files;

        InputStream i = new ByteArrayInputStream(photo);


        Response res = new Response( HTTP_OK, "image/jpeg", i);
        click = true;
        return res;
    }
    
    @Override
    public void serveDone(Response r) {
       try{
            if ( r.isStreaming ) { 
                r.data.close();
            }
       } catch(IOException ex) {
       }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        if(click == true) {

            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);
            int lenth = image.getHeight() * image.getWidth();
            filecon = new ByteArrayOutputStream(lenth);
            image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, filecon);
            photo = filecon.toByteArray();

        }
        click = false;





    }

    public static interface CommonGatewayInterface {
        public String run(Properties parms); 
        public InputStream streaming(Properties parms);
    }
    private HashMap<String, CommonGatewayInterface> cgiEntries = new HashMap<String, CommonGatewayInterface>();
    public void registerCGI(String uri, CommonGatewayInterface cgi) {
        if ( cgi != null)
			cgiEntries.put(uri, cgi);
    }

}
