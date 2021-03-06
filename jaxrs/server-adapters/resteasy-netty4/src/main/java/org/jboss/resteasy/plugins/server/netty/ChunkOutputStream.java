package org.jboss.resteasy.plugins.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;

import java.io.IOException;
import java.io.OutputStream;
/**
 * Class to help application that are built to write to an
 * OutputStream to chunk the content
 *
 * <pre>
 * {@code
DefaultHttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
HttpHeaders.setTransferEncodingChunked(response);
response.headers().set(CONTENT_TYPE, "application/octet-stream");
//other headers
ctx.write(response);
// code of the application that use the ChunkOutputStream
// Don't forget to close the ChunkOutputStream after use!
ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
}
 </pre>
 * @author tbussier
 *
 */
public class ChunkOutputStream extends OutputStream {
   final ByteBuf buffer;
   final ChannelHandlerContext ctx;
   final NettyHttpResponse response;

   ChunkOutputStream(NettyHttpResponse response, ChannelHandlerContext ctx, int chunksize) {
      this.response = response;
      if (chunksize < 1) {
         throw new IllegalArgumentException("Chunk size must be at least 1");
      }
      this.buffer = Unpooled.buffer(0, chunksize);
      this.ctx = ctx;
   }

   @Override
   public void write(int b) throws IOException {
      if (buffer.maxWritableBytes() < 1) {
         flush();
      }
      buffer.writeByte(b);
   }

   public void reset()
   {
      if (response.isCommitted()) throw new IllegalStateException("response is committed");
      buffer.clear();
   }

   @Override
   public void close() throws IOException {
      flush();
      super.close();
   }


   @Override
   public void write(byte[] b, int off, int len) throws IOException {
      int dataLengthLeftToWrite = len;
      int dataToWriteOffset = off;
      int spaceLeftInCurrentChunk;
      while ((spaceLeftInCurrentChunk = buffer.maxWritableBytes()) < dataLengthLeftToWrite) {
         buffer.writeBytes(b, dataToWriteOffset, spaceLeftInCurrentChunk);
         dataToWriteOffset = dataToWriteOffset + spaceLeftInCurrentChunk;
         dataLengthLeftToWrite = dataLengthLeftToWrite - spaceLeftInCurrentChunk;
         flush();
      }
      if (dataLengthLeftToWrite > 0) {
         buffer.writeBytes(b, dataToWriteOffset, dataLengthLeftToWrite);
      }
   }

   @Override
   public void flush() throws IOException {
      if (!buffer.isWritable()) return;
      if (!response.isCommitted()) response.prepareChunkStream();
      ctx.writeAndFlush(new DefaultHttpContent(buffer.copy()));
      buffer.clear();
      super.flush();
   }

}