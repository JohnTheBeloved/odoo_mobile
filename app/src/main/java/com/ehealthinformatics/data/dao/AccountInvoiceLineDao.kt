package com.ehealthinformatics.data.dao

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ehealthinformatics.App

import com.ehealthinformatics.BuildConfig
import com.ehealthinformatics.data.dto.*
import com.ehealthinformatics.core.orm.ODataRow
import com.ehealthinformatics.core.orm.OModel
import com.ehealthinformatics.core.orm.fields.OColumn
import com.ehealthinformatics.core.orm.fields.types.OFloat
import com.ehealthinformatics.core.orm.fields.types.OVarchar
import com.ehealthinformatics.core.support.OUser
import com.ehealthinformatics.data.LazyList
import com.ehealthinformatics.core.orm.fields.OColumn.RelationType
import com.ehealthinformatics.data.LazyList.ItemFactory
import com.ehealthinformatics.data.db.Columns
import com.ehealthinformatics.data.db.ModelNames

class AccountInvoiceLineDao(context: Context?, user: OUser?) : OModel(context, ModelNames.POS_ORDER_LINE, user) {

    internal var company_id = OColumn("Company", ResCompany::class.java, RelationType.ManyToOne)
    internal var name = OColumn("Name", OVarchar::class.java).setSize(100).setRequired()
    internal var notice = OColumn("Notice", OVarchar::class.java)
    internal var product_id = OColumn("Product", ProductDao::class.java, RelationType.ManyToOne)
    internal var price_unit = OColumn("Unit Price", OFloat::class.java)
    internal var qty = OColumn("Quantity", OFloat::class.java)
    internal var price_subtotal = OColumn("Subtotal w/o Tax", OFloat::class.java)
    internal var price_subtotal_incl = OColumn("Subtotal with Tax", OFloat::class.java)
    internal var discount = OColumn("Discount", OFloat::class.java)
    internal var order_id = OColumn("Order", PosOrderDao::class.java, RelationType.ManyToOne)
    lateinit var companyDao: ResCompany
    lateinit var productDao: ProductDao
    lateinit var posOrderDao: PosOrderDao

   fun  posOrderLineCreator(queryFields: QueryFields): ItemFactory<PosOrderLine> {
       return object : ItemFactory<PosOrderLine> {
           override fun create(id: Int): PosOrderLine {
               return get(id,queryFields) as PosOrderLine
           }
       }
   }

    override fun initDaos() {
        companyDao = App.getDao(ResCompany::class.java)
        productDao = App.getDao(ProductDao::class.java)
        posOrderDao = App.getDao(PosOrderDao::class.java)
    }

    init {
        setHasMailChatter(true)
    }

    fun fromPosOrder(posOrder: PosOrder, queryFields: QueryFields): List<PosOrderLine> {
        return select(null, "order_id = ?",  arrayOf("${posOrder.id}")).map { fromRow(it, posOrder, queryFields) }
    }

    fun fromRow(row: ODataRow, order: PosOrder?, qf: QueryFields): PosOrderLine {
        val id = if(qf.contains(Columns.id)) row.getInt(Columns.id) else null
        val name = if(qf.contains(Columns.name)) row.getString(Columns.name) else null
        val notice = if(qf.contains(Columns.PosOrderLine.notice)) row.getString(Columns.PosOrderLine.notice) else null
        val unitPrice = if(qf.contains(Columns.PosOrderLine.price_unit)) row.getFloat(Columns.PosOrderLine.price_unit) else null
        val quantity = if(qf.contains(Columns.PosOrderLine.qty)) row.getFloat(Columns.PosOrderLine.qty) else null
        val subTotal = if(qf.contains(Columns.PosOrderLine.price_subtotal)) row.getFloat(Columns.PosOrderLine.price_subtotal) else null
        val subTotalIncl = if(qf.contains(Columns.PosOrderLine.price_subtotal_incl)) row.getFloat(Columns.PosOrderLine.price_subtotal_incl) else null
        val discount = if(qf.contains(Columns.PosOrderLine.discount)) row.getFloat(Columns.PosOrderLine.discount) else null
        val realPosOrder = if (order?.id == null) { posOrderDao.get(row.getInt(Columns.PosOrderLine.order_id), qf.childField(Columns.PosOrderLine.order_id)) } else {  order }
        val company: Company? = if(qf.contains(Columns.PosOrderLine.company_id)) companyDao.get(row.getInt(Columns.PosOrderLine.company_id), qf.childField(Columns.AccountInvoice.company_id))  else null
        val product = if(qf.contains(Columns.PosOrderLine.product_id)) productDao.get(row.getInt(Columns.PosOrderLine.product_id) , qf.childField(Columns.name)) else null
        return PosOrderLine(id, name, company, product, notice, unitPrice, quantity,
                subTotal, subTotalIncl, discount, realPosOrder)
    }

    override fun uri(): Uri {
        return buildURI(AUTHORITY)
    }

    fun selectAll(context: Context, uri: Uri, projection: Array<String>, selection: String,
                                    selectionArgs: Array<String>, sortOrder: String, queryFields: QueryFields, order: PosOrder): LazyList<PosOrderLine> {
        return  LazyList(posOrderLineCreator(queryFields), select(arrayOf(Columns.id), selection, selectionArgs, sortOrder))
    }

    override fun onSyncStarted() {
        Log.e(TAG, "PosOrderLineDao->onSyncStarted")
    }

    override fun onSyncFinished() {
        Log.e(TAG, "PosOrderLineDao->onSyncFinished")
    }

    companion object {
        val TAG = AccountInvoiceLineDao::class.java.simpleName
        @JvmField
        var AUTHORITY = BuildConfig.APPLICATION_ID + ".core.provider.content.sync.pos_order_line"
    }
}
