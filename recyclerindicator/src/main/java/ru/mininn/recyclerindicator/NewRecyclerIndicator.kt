package ru.mininn.recyclerindicator

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.PorterDuff
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlin.math.abs

class NewRecyclerIndicator(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val SCALE_STATE_NORMAL = 0.5f
    private val SCALE_STATE_SELECTED = 1f
    private val SCALE_STATE_SMALL = 0.2f
    private val SCALE_STATE_INVISIBLE = 0.0f
    private val INDICATOR_SIZE = 12
    private val INDICATOR_MARGIN = 2

    private var currentPosition = -1
    private var recyclerView: RecyclerView? = null
    var maxItemCount: Int = 5
    var indicatorSize = 10
    var indicatorMargin = 2
    var drawable: Int = R.drawable.dot

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        initTransitions()
        addAdapterDataObserver(recyclerView)
        addScrollListener(recyclerView)
        val displayMetrics = resources.displayMetrics
        this.indicatorSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, INDICATOR_SIZE.toFloat(), displayMetrics).toInt()
        this.indicatorMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, INDICATOR_MARGIN.toFloat(), displayMetrics).toInt()
        updateIndicatorsCount(recyclerView.adapter.itemCount)
    }

    fun setPosition(position: Int) {
        if (currentPosition == position) {
            return
        } else {
            currentPosition = position
            scaleItems(true)
        }

    }

    fun updateIndicatorsCount(itemCount: Int) {
        removeAllViews()
        if (currentPosition < 0) {
            currentPosition = (recyclerView!!.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        }
        var x = 0
        while (x < itemCount) {
            addIndicator(x)
            x++
        }
        scaleItems(true)
    }

    private fun scaleItems(animate: Boolean) {
        when {
            childCount <= 0 -> return
            childCount <= maxItemCount -> normalScaling(animate)
            else -> infinityScaling(animate)
        }

    }

    private fun infinityScaling(animate: Boolean) {
        for (i in 0 until childCount) {
            when {
                currentPosition < 3 -> {
                    when {
                        i == currentPosition -> scaleView(getChildAt(i), SCALE_STATE_SELECTED, animate)
                        currentPosition != i && i <= 3 -> scaleView(getChildAt(i), SCALE_STATE_NORMAL, animate)
                        currentPosition != i && i == 4 -> scaleView(getChildAt(i), SCALE_STATE_SMALL, animate)
                        else -> getChildAt(i).visibility = View.GONE
                    }
                }
                childCount - currentPosition <= 3 -> {
                    when {
                        i == currentPosition -> scaleView(getChildAt(i), SCALE_STATE_SELECTED, animate)
                        currentPosition != i && childCount - i <= 4 -> scaleView(getChildAt(i), SCALE_STATE_NORMAL, animate)
                        currentPosition != i && childCount - i == 5 -> scaleView(getChildAt(i), SCALE_STATE_SMALL, animate)
                        else -> getChildAt(i).visibility = View.GONE
                    }
                }
                else -> {
                    when {
                        i == currentPosition -> {
                            scaleView(getChildAt(i), SCALE_STATE_SELECTED, animate)
                        }
                        abs(i - currentPosition) == 1 -> {
                            scaleView(getChildAt(i), SCALE_STATE_NORMAL, animate)
                        }
                        abs(i - currentPosition) == 2 -> {
                            scaleView(getChildAt(i), SCALE_STATE_SMALL, animate)
                        }
                        abs(i - currentPosition) > 2 -> {
                            scaleView(getChildAt(i), SCALE_STATE_INVISIBLE, animate)
                        }
                    }
                }
            }

        }
    }

    private fun normalScaling(animate: Boolean) {

        for (i in 0 until childCount) {
            if (currentPosition == i) {
                scaleView(getChildAt(i), SCALE_STATE_SELECTED, animate)
            } else {
                scaleView(getChildAt(i), SCALE_STATE_NORMAL, animate)
            }
        }
    }

    private fun addIndicator(position: Int) {
        val view = View(context)
        val params = ViewGroup.MarginLayoutParams(indicatorSize, indicatorSize)
        val adapter = recyclerView!!.adapter as IndicatorAdapter
        view.background = context.resources.getDrawable(drawable).mutate()
        view.background.setColorFilter(adapter.getItemColor(position), PorterDuff.Mode.MULTIPLY)
        params.leftMargin = indicatorMargin
        params.rightMargin = indicatorMargin
        params.topMargin = indicatorMargin
        params.bottomMargin = indicatorMargin
        addView(view, params)
        scaleView(view, SCALE_STATE_INVISIBLE, false)
    }

    private fun scaleView(view: View, scale: Float, animate: Boolean) {
        if (scale == SCALE_STATE_INVISIBLE) {
            view.scaleX = scale
            view.scaleY = scale
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
            if (animate) {
                view.animate().scaleY(scale).scaleX(scale)
            } else {
                view.scaleX = scale
                view.scaleY = scale
            }
        }
    }

    private fun initTransitions() {
        this.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        this.layoutTransition.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
        this.layoutTransition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
    }

    private fun addScrollListener(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = (recyclerView!!.layoutManager as LinearLayoutManager)
                val position = if (layoutManager.findFirstCompletelyVisibleItemPosition() >= 0) {
                    layoutManager.findFirstCompletelyVisibleItemPosition()
                } else {
                    if (layoutManager.findFirstVisibleItemPosition() < currentPosition) {
                        layoutManager.findLastVisibleItemPosition()
                    } else {
                        layoutManager.findFirstVisibleItemPosition()
                    }
                }

                if (position != currentPosition && position >= 0) {
                    setPosition(position)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == 0) {
                    currentPosition = (recyclerView!!.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                }
            }
        })
    }

    private fun addAdapterDataObserver(recyclerView: RecyclerView) {
        recyclerView.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                recyclerView.postDelayed({
                    currentPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    updateIndicatorsCount(recyclerView.adapter.itemCount)
                }, 150)

            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                currentPosition = positionStart
                updateIndicatorsCount(recyclerView.adapter.itemCount)
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                currentPosition = positionStart
                updateIndicatorsCount(recyclerView.adapter.itemCount)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                updateIndicatorsCount(recyclerView.adapter.itemCount)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                updateIndicatorsCount(recyclerView.adapter.itemCount)
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                updateIndicatorsCount(recyclerView.adapter.itemCount)
            }
        })
    }

}