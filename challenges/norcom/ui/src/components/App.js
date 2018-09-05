
import React from 'react';
import DataService from '../services/data/index';
import { Switch, Route, Redirect } from 'react-router-dom';

import Objs from './Objs';
import Obj from './Obj';
import Header from './Header';

class App extends React.Component {
	
	constructor(props){
		super(props)
		this.app = {
			services: {
				data: new DataService(props.configuration)
			}
			, configuration : props.configuration
		};
		this.state = {
			objs: []
			, page: null
			, obj: null
			, search: null
		};
		this.search = this.search.bind(this);
	}
	
	change(objs){
		this.setState({objs: objs});
	}
	
	search(term){
		console.log('[App|search|in] term:', term);
		this.app.services.data.search(term, (e,os) => { this.change(os) });	
		console.log('[App|search|out]');
	}
	
	render(){
		console.log('[App|render|in]');
		console.log('objs', this.state.objs);
		console.log('state', this.state);
		console.log('[App|render|out]');

		const { app, state } = this;
		const functions = { search: this.search };
		const objs = this.state.objs;
		return (
            <section className="container-fluid">
				<Header state={state} functions={functions} />
				<section className="container">
					<Switch>
						<Route path='/objs/:id' render={(props) => <Obj {...props} state={state} app={app} />} />	
						<Route path='/objs' render={ (props) => <Objs {...props} objs={this.state.objs} /> } />
						<Redirect from='/' to='/objs' />
					</Switch>
				</section>
			</section>
		)
	}
};

export default App;

