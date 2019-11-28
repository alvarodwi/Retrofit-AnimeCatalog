package com.pedo.animecatalog.ui.favorite


import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.pedo.animecatalog.R
import com.pedo.animecatalog.databinding.FragmentAnimeFavoriteBinding
import com.pedo.animecatalog.utils.AnimeListingStatus
import com.pedo.animecatalog.utils.TYPE_GRID
import com.pedo.animecatalog.utils.TYPE_LIST
import com.pedo.animecatalog.utils.adapter.AnimeListAdapter
import com.pedo.animecatalog.utils.determineGridSpan

class AnimeFavoriteFragment : Fragment() {
    private val viewModel: AnimeFavoriteViewModel by lazy {
        ViewModelProviders.of(
            this,
            AnimeFavoriteViewModel.Factory("tv", activity!!.application)
        ).get(AnimeFavoriteViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = requireNotNull(activity)
        val binding = FragmentAnimeFavoriteBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.viewMode.observe(this, Observer { type ->
            type?.let {
                when (it) {
                    TYPE_LIST -> {
                        binding.animeFavoriteRv.layoutManager =
                            LinearLayoutManager(activity.applicationContext)
                        binding.animeFavoriteRv.adapter =
                            AnimeListAdapter(
                                AnimeListAdapter.OnClickListener {
                                    viewModel.displayMovieDetail(it)
                                })
                    }
                    TYPE_GRID -> {
                        binding.animeFavoriteRv.layoutManager = GridLayoutManager(
                            activity.applicationContext,
                            determineGridSpan(activity.applicationContext)
                        )
                        binding.animeFavoriteRv.adapter =
                            AnimeListAdapter(
                                AnimeListAdapter.OnClickListener {
                                    viewModel.displayMovieDetail(it)
                                }, TYPE_GRID
                            )
                    }
                }
            }
        })

        viewModel.navigateToDetail.observe(this, Observer { anime ->
            anime?.let {
                this.findNavController().navigate(
                    AnimeFavoriteFragmentDirections.showDetail(it)
                )
                viewModel.displayMovieDetailCompleted()
            }
        })

        binding.srlFavorites.setOnRefreshListener {
            viewModel.onRefresh()
        }

        viewModel.status.observe(this, Observer {
            it?.let { status ->
                when (status) {
                    AnimeListingStatus.LOADING -> binding.srlFavorites.isRefreshing = true
                    AnimeListingStatus.DONE, AnimeListingStatus.ERROR -> binding.srlFavorites.isRefreshing =
                        false
                }
            }
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.onRefresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.info -> {
                Toast.makeText(activity, "About Click!", Toast.LENGTH_SHORT).show()
            }
            R.id.list -> {
                viewModel.changeViewType(TYPE_LIST)
            }
            R.id.grid -> {
                viewModel.changeViewType(TYPE_GRID)
            }
        }
        return true
    }

}
