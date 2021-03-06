package com.jjosft.android.lottovillage.activities

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.jjosft.android.lottovillage.R
import com.jjosft.android.lottovillage.adapters.PointHistoryAdapter
import com.jjosft.android.lottovillage.base.BaseActivity
import com.jjosft.android.lottovillage.base.BaseApplication
import com.jjosft.android.lottovillage.model.Model
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_point_history.*
import kotlinx.android.synthetic.main.content_point_history.*

class PointHistoryActivity : BaseActivity() {
    private val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_history)
        setSupportActionBar(point_history_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val yearAdapter = ArrayAdapter<String>(applicationContext, R.layout.spinner_lotto, resources.getStringArray(R.array.years))
        point_history_spinner_year.adapter = yearAdapter
        point_history_spinner_year.setSelection(0)

        val monthAdapter = ArrayAdapter<String>(applicationContext, R.layout.spinner_lotto, resources.getStringArray(R.array.months))
        point_history_spinner_month.adapter = monthAdapter
        point_history_spinner_month.setSelection(0)

        retrievePointHistory(point_history_spinner_year.selectedItem.toString() + point_history_spinner_month.selectedItem.toString())
    }

    override fun onStop() {
        super.onStop()
        mCompositeDisposable.clear()
    }

    fun customOnClick(view: View) {
        when (view.id) {
            R.id.point_history_button_retrieve -> {
                retrievePointHistory(point_history_spinner_year.selectedItem.toString() + point_history_spinner_month.selectedItem.toString())
            }
        }
    }

    private fun retrievePointHistory(savingTime: String) {
        BaseApplication.getInstance().getRetrofitMethod().getDetailsOfPointHistory("20$savingTime")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Model.PointHistoryResponse> {
                    override fun onSubscribe(d: Disposable) {
                        mCompositeDisposable.add(d)
                        progressOn("로또 참여 내역을 불러오는 중 입니다")
                    }

                    override fun onNext(t: Model.PointHistoryResponse) {
                        if (t.isSuccess) {
                            val pointHistoryAdapter = PointHistoryAdapter(applicationContext, t.detailsOfPointHistory)
                            point_history_recycler_view.layoutManager = LinearLayoutManager(applicationContext)
                            point_history_recycler_view.adapter = pointHistoryAdapter
                        } else {
                            Toast.makeText(applicationContext, t.errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(applicationContext, "실패 ${e.message}", Toast.LENGTH_SHORT).show()
                        progressOff()
                        mCompositeDisposable.clear()
                    }

                    override fun onComplete() {
                        progressOff()
                        mCompositeDisposable.clear()
                    }

                })
    }
}