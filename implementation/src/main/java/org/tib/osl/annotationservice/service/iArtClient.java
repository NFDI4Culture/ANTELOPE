package org.tib.osl.annotationservice.service;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import org.h2.command.ddl.Analyze;
import org.wildfly.common.bytes.ByteStringBuilder;

import iart.client.*;

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

  public static List<iart.client.PluginResult> analyze(String imageModel, String base64imageString) {
    try {
      ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build();

      IndexerGrpc.IndexerBlockingStub stub = IndexerGrpc.newBlockingStub(channel);
      
      /*
      // load local file
      Path imagePath = Paths.get("./src/main/webapp/content/images/landscape.jpg");
      File imageFile = imagePath.toFile();
      byte[] imageBytes;

      FileInputStream fileInputStream = new FileInputStream(imageFile);
      imageBytes = fileInputStream.readAllBytes();
      */
      if( imageModel == null) {
        imageModel = "KaggleResnetClassifier";
      }

      AnalyzeReply response = stub.analyze(AnalyzeRequest.newBuilder()
        //.setImage(com.google.protobuf.ByteString.copyFrom(imageBytes))
        .setImage(com.google.protobuf.ByteString.copyFromUtf8(base64imageString))
        .addPluginNames(imageModel)
        .build());

      System.out.println(  response.toString());
      channel.shutdown();
      return response.getResultsList();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
