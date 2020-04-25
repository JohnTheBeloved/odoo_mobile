package com.ehealthinformatics.data.dto

import com.ehealthinformatics.core.orm.OValues
import com.ehealthinformatics.core.orm.RelValues
import com.ehealthinformatics.data.db.Columns
import java.util.ArrayList

class PosConfig(var id: Int?, var serverId: Int?, var name: String?, var locationId: Int?, var company: Company?, var priceList: PriceList?, var journal: AccountJournal?, var paymentJournals: List<AccountJournal>) : DTO {

    override fun toOValues(): OValues {
        var oValues = OValues()
        oValues.put(Columns.name, name)
        oValues.put(Columns.server_id, serverId)
        oValues.put(Columns.PosConfig.company_id, company?.id)
        oValues.put(Columns.PosConfig.journal_id, journal?.id)
        oValues.put(Columns.PosConfig.journal_ids, toRelValues(paymentJournals))
        return oValues
    }

    fun toRelValues(paymentJournals: List<AccountJournal>) : RelValues {
        var ids = ArrayList<Int>()
        val relValues = RelValues()
        for (paymentJournal in paymentJournals) {
            ids.add(paymentJournal.id)
        }
        relValues.append(ids.toArray())
        return relValues
    }
}