package org.tib.osl.annotationservice.service;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import org.h2.command.ddl.Analyze;
import org.wildfly.common.bytes.ByteStringBuilder;

import com.google.protobuf.ByteString;

import iart.client.*;
import iart.indexer.Data.BoundingBox;
import iart.indexer.Data.ImageData;
import iart.indexer.Data.PluginData;
import iart.indexer.Data.StringData;

public class iArtClient {
  private static int port = 50051;
  private static String host = "localhost";
    
  public static void main(String[] args) {
    
  }


  public static String getStatus() {
    ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
      .usePlaintext()
      .build();
    IndexerGrpc.IndexerBlockingStub stub = IndexerGrpc.newBlockingStub(channel);
    StatusReply response = stub.status(StatusRequest.newBuilder().build());
    channel.shutdown();
    return response.toString();
  }

  public static List<iart.client.PluginInfo> getPluginList() {
    ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
      .usePlaintext()
      .build();
    IndexerGrpc.IndexerBlockingStub stub = IndexerGrpc.newBlockingStub(channel);
    ListPluginsReply response = stub.listPlugins(ListPluginsRequest.newBuilder().build());
    channel.shutdown();
    return response.getPluginsList();
  }

  public static List<iart.client.PluginResult> analyze(String imageModel, byte[] image, List<String> dict) {
    try {
      ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build();

      IndexerGrpc.IndexerBlockingStub stub = IndexerGrpc.newBlockingStub(channel);


      //ImageData imageData = ImageData.newBuilder().setContent(ByteString.copyFrom(base64imageString.getBytes())).setType("image").build();
      ImageData imageData = ImageData.newBuilder().setContent(ByteString.copyFrom(image)).setType("image").build();
      AnalyseRequest.Builder requestBuilder= AnalyseRequest.newBuilder();
        
        
      requestBuilder.addInputs(
        PluginData.newBuilder().setName("image").setImage(imageData).build()
      );
        
      for( String word : dict) {
        requestBuilder.addInputs(
          PluginData.newBuilder().setName("text").setString(StringData.newBuilder().setText( word ).build()).build()
        );
      }

      requestBuilder.addInputs(
        PluginData.newBuilder().setName("text").setString(StringData.newBuilder().setText("other").build()).build()
      );

             
      requestBuilder.setPlugin(imageModel);
        

      AnalyseReply response = stub.analyse(requestBuilder.build());
      System.out.println(  response.toString());
      channel.shutdown();
      return response.getResultsList();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
