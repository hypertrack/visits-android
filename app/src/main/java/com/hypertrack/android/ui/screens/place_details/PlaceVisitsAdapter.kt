package com.hypertrack.android.ui.screens.place_details

import android.view.View
import com.hypertrack.android.api.GeofenceMarker
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.common.*
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.android.utils.stringFromResource
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_place_visit.view.*
import java.time.ZoneId
import java.time.ZonedDateTime

class PlaceVisitsAdapter(
    private val osUtilsProvider: OsUtilsProvider,
    private val onCopyClickListener: ((String) -> Unit)? = null
) : BaseAdapter<GeofenceMarker, BaseAdapter.BaseVh<GeofenceMarker>>() {

    override val itemLayoutResource: Int = R.layout.item_place_visit

    override fun createViewHolder(
        view: View,
        baseClickListener: (Int) -> Unit
    ): BaseVh<GeofenceMarker> {
        return object : BaseContainerVh<GeofenceMarker>(view, baseClickListener) {
            override fun bind(item: GeofenceMarker) {
                formatDate(
                    item.arrival!!.recordedAt,
                    item.exit?.recordedAt
                ).toView(containerView.tvTitle)
                item.duration?.let { DateTimeUtils.secondsToLocalizedString(it) }
                    ?.toView(containerView.tvDescription)
                item.markerId.toView(containerView.tvVisitId)
                item.routeTo?.let {
                    if (it.distance == null) return@let null
                    if (it.duration == null) return@let null
                    MyApplication.context.getString(
                        R.string.place_route_ro,
                        DistanceUtils.metersToDistanceString(it.distance),
                        DateTimeUtils.secondsToLocalizedString(it.duration)
                    )
                }?.toView(containerView.tvRouteTo)
                listOf(containerView.ivRouteTo, containerView.tvRouteTo).forEach {
                    it.setGoneState(item.routeTo == null)
                }

                containerView.divider.setGoneState(adapterPosition == itemCount - 1)

                containerView.setOnClickListener {
                    onCopyClickListener?.invoke(item.markerId)
                }
            }
        }
    }

    //todo test
    fun formatDate(enter: String?, exit: String?): String {
        val enterDt = ZonedDateTime.parse(enter)
        val exitDt = exit?.let { ZonedDateTime.parse(it) }
        val equalDay = enterDt.dayOfMonth == exitDt?.dayOfMonth


        //todo now
        return if (equalDay) {
            "${getDateString(enterDt)}, ${getTimeString(enterDt)} — ${getTimeString(exitDt)}"
        } else {
            "${getDateString(enterDt)}, ${getTimeString(enterDt)} — ${
                exitDt?.let {
                    "${getDateString(exitDt)}, ${getTimeString(exitDt)}"
                } ?: osUtilsProvider.getString(R.string.now)
            }"
        }
    }

    private fun getDateString(it: ZonedDateTime): String {
        val now = ZonedDateTime.now()
        val yesterday = ZonedDateTime.now().minusDays(1)
        return when {
            isSameDay(it, now) -> {
                osUtilsProvider.stringFromResource(R.string.place_today)
            }
            isSameDay(it, yesterday) -> {
                osUtilsProvider.stringFromResource(R.string.place_yesterday)
            }
            else -> {
                it.formatDate()
            }
        }
    }

    private fun getTimeString(it: ZonedDateTime?): String {
        return it?.formatTime() ?: osUtilsProvider.getString(R.string.now)
    }

    private fun isSameDay(date1: ZonedDateTime, date2: ZonedDateTime): Boolean {
        val d1 = date1.withZoneSameInstant(ZoneId.of("UTC"))
        val d2 = date2.withZoneSameInstant(ZoneId.of("UTC"))
        return d1.dayOfMonth == d2.dayOfMonth && d1.year == d2.year
    }
}