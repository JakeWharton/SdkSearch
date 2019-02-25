package com.jakewharton.sdksearch.proxy

import com.google.common.base.Suppliers
import com.jakewharton.sdksearch.proxy.model.DocumentedType
import java.util.concurrent.TimeUnit.MINUTES
import javax.inject.Singleton
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType.APPLICATION_JSON

@Singleton
@Path("")
internal class ApiResource {
  private val cachedList = Suppliers.memoizeWithExpiration(DocumentedTypeListSupplier, 30, MINUTES)

  @GET
  @Path("list")
  @Produces(APPLICATION_JSON)
  fun list(): List<DocumentedType> {
    return cachedList.get()
  }
}
