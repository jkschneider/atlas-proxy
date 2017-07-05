import React, { Component } from 'react';
import PropTypes from 'prop-types';
import 'bootstrap/dist/js/bootstrap.min.js'; // for dropdown
import Clipboard from 'clipboard';

export default class AtlasGraph extends Component {
  constructor(props) {
    super(props);
    this.state = {};
    this.explode = this.explode.bind(this);
  }

  componentDidMount() {
    new Clipboard('.clipme');
  }

  explode(tag) {
    let tagValues = this.props.graph.explodableTags.find(t => t.name === tag);
    this.setState({ explode: tagValues });
  }

  render() {
    const graph = this.props.graph;
    const explode = this.state.explode;

    let grid = (arr, n) => new Array(Math.ceil(arr.length/n)).fill().map((_,i) => arr.slice(i*n,i*n+n));

    return (
      <div>
        <label style={{ marginRight: 10, fontWeight: 'bold' }}>Graph Actions</label>
        <div className="btn-group" role="group">
          <button className="btn btn-sm btn-secondary clipme" data-clipboard-text={graph.query}>Copy Stack Query
          </button>
          {graph.explodableTags.length > 0 ?
            <div className="dropdown">
              <button className="btn btn-sm btn-secondary dropdown-toggle" type="button" data-toggle="dropdown"
                      aria-haspopup="true">Explode by
              </button>
              <div className="dropdown-menu">
                {graph.explodableTags.map(t => t.name).map(t => <a key={t} className="dropdown-item"
                                                                   onClick={() => this.explode(t)}>{t}</a>)}
              </div>
            </div>
            : ''}
        </div>
        <img className="img-fluid" src={graph.source} alt="Graph" />
        {explode ?
          <div>
            <h3 className="mt-4">Exploded by {explode.name}</h3>
            {grid(explode.values, 2).map((values, i) =>
              <div key={`explode-row-${i}`} className="row mt-3">
                {values.map(v => {
                  let src = graph.source + `,:list,(,${explode.name},${v},:eq,:cq,),:each&no_legend=1`;
                  let viewportWidth = src.match(/w=(\d+)/)[1];
                  src = src.replace(/w=\d+/, `w=${viewportWidth/2}`);

                  return (
                    <div key={v} className="col-sm-6">
                      <h4>{v}</h4>
                      <img className="img-fluid" src={src} alt={v} />
                    </div>
                  )
                })}
              </div>,
            )}
          </div> : '' }
      </div>
    );
  }
}

AtlasGraph.propTypes = {
  graph: PropTypes.shape({
    source: PropTypes.string.isRequired,
    query: PropTypes.string.isRequired,
    explodableTags: PropTypes.arrayOf(
      PropTypes.shape({
        name: PropTypes.string.isRequired,
        values: PropTypes.arrayOf(PropTypes.string).isRequired,
      }),
    ),
  }),
  atlasUri: PropTypes.string.isRequired,
};
