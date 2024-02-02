package org.tib.osl.annotationservice.service;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;


import com.google.protobuf.ByteString;

import iart.client.*;
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
      AnalyseRequest.Builder requestBuilder= AnalyseRequest.newBuilder();

      ImageData imageData = null;
      if( image != null){
        imageData = ImageData.newBuilder().setContent(ByteString.copyFrom(image)).setType("image").build();
        requestBuilder.addInputs(
          PluginData.newBuilder().setName("image").setImage(imageData).build()
        );
      }
      
      if(dict != null) { 
        for( String word : dict) {
          requestBuilder.addInputs(
            PluginData.newBuilder().setName("text").setString(StringData.newBuilder().setText( word ).build()).build()
          );
        }
      }
             
      requestBuilder.setPlugin(imageModel);
        
      AnalyseReply response = stub.analyse(requestBuilder.build());
      System.out.println( response.toString());
      channel.shutdown();
      return response.getResultsList();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
